package com.Tu.Tu.service;


import com.Tu.Tu.constant.BookingStatus;
import com.Tu.Tu.constant.DepartureStatus;
import com.Tu.Tu.constant.PaymentStatus;
import com.Tu.Tu.dto.request.BookingCreateRequest;
import com.Tu.Tu.dto.request.BookingUpdateRequest;
import com.Tu.Tu.dto.request.TravelerUpdateRequest;
import com.Tu.Tu.dto.response.BookingResponse;
import com.Tu.Tu.dto.response.PageResponse;
import com.Tu.Tu.dto.response.TravelerResponse;
import com.Tu.Tu.entity.*;
import com.Tu.Tu.exception.AppException;
import com.Tu.Tu.exception.ErrorCode;
import com.Tu.Tu.mapper.BookingMapper;
import com.Tu.Tu.mapper.TravelerMapper;
import com.Tu.Tu.repository.*;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingService {

    BookingRepository bookingRepository;
    DepartureRepository departureRepository;
    PassengerTypeRepository passengerTypeRepository;
    TravelerRepository travelerRepository;
    UserRepository userRepository;
    BookingMapper bookingMapper;
    TravelerMapper travelerMapper;

    // ===== CUSTOMER =====

    @Transactional
    public BookingResponse createBooking(BookingCreateRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Departure departure = departureRepository.findById(request.getDepartureId())
                .orElseThrow(() -> new AppException(ErrorCode.DEPARTURE_NOT_FOUND));

        if (departure.getDepartureDate() != null && departure.getDepartureDate().isBefore(LocalDate.now())) {
            throw new AppException(ErrorCode.DEPARTURE_ALREADY_DEPARTED);
        }

        if (DepartureStatus.CANCELLED.equalsIgnoreCase(String.valueOf(departure.getStatus()))) {
            throw new AppException(ErrorCode.BOOKING_INVALID_STATUS);
        }

        int remainingSeats = departure.getCapacity() - departure.getBookedSeats();
        if (remainingSeats < request.getTravelerCreateRequestList().size()) {
            throw new AppException(ErrorCode.DEPARTURE_FULL);
        }

        long totalAmount = 0;
        for (var travelerReq : request.getTravelerCreateRequestList()) {
            PassengerType passengerType = passengerTypeRepository.findById(travelerReq.getPassengerTypeId())
                    .orElseThrow(() -> new AppException(ErrorCode.PASSENGER_TYPE_NOT_FOUND));

            DeparturePrice departurePrice = departure.getDeparturePriceList().stream()
                    .filter(p -> p.getPassengerType().getId().equals(passengerType.getId()))
                    .findFirst()
                    .orElseThrow(() -> new AppException(ErrorCode.BOOKING_PRICE_NOT_FOUND));

            totalAmount += departurePrice.getPrice();
        }

        Booking booking = Booking.builder()
                .bookingCode(generateBookingCode())
                .contactName(request.getContactName())
                .contactPhone(request.getContactPhone())
                .contactEmail(request.getContactEmail())
                .specialRequest(request.getSpecialRequest())
                .totalAmount(totalAmount)
                .createAt(LocalDate.now())
                .status(BookingStatus.PENDING)
                .user(user)
                .departure(departure)
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        List<Traveler> travelerList = request.getTravelerCreateRequestList().stream()
                .map(t -> {
                    PassengerType passengerType = passengerTypeRepository.findById(t.getPassengerTypeId()).orElseThrow();
                    return Traveler.builder()
                            .fullName(t.getFullName())
                            .dob(t.getDob())
                            .passengerType(passengerType)
                            .booking(savedBooking)
                            .build();
                }).toList();

        travelerRepository.saveAll(travelerList);

        departure.setBookedSeats(departure.getBookedSeats() + travelerList.size());
        if (departure.getBookedSeats().equals(departure.getCapacity())) {
            departure.setStatus(DepartureStatus.FULL);
        }
        departureRepository.save(departure);

        savedBooking.setTravelerList(travelerList);
        return toResponseWithPrice(savedBooking, departure);
    }

    public BookingResponse getMyBookingById(Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (!booking.getUser().getEmail().equals(email)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        return toResponseWithPrice(booking, booking.getDeparture());
    }

    public PageResponse<BookingResponse> getMyBookings(int page, int size) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Booking> result = bookingRepository.findByUserId(user.getId(), pageable);
        return toPageResponse(result);
    }

    public PageResponse<BookingResponse> searchMyBookings(String keyword, String status,
                                                          LocalDate fromDate, LocalDate toDate,
                                                          int page, int size) {
        // Validate date range
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Pageable pageable = PageRequest.of(page, size);
        Page<Booking> result = bookingRepository.searchMyBookings(
            user.getId(),
            normalizeKeyword(keyword),
            status,
            fromDate,
            toDate,
            pageable
        );
        return toPageResponse(result);
    }

    @Transactional
    public BookingResponse updateBooking(Long id, BookingUpdateRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (!booking.getUser().getEmail().equals(email)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (BookingStatus.CONFIRMED.equals(booking.getStatus())) {
            throw new AppException(ErrorCode.BOOKING_ALREADY_CONFIRMED);
        }

        bookingMapper.updateBooking(request, booking);
        return toResponseWithPrice(bookingRepository.save(booking), booking.getDeparture());
    }

    @Transactional
    public BookingResponse updateTraveler(Long bookingId, Long travelerId, TravelerUpdateRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (!booking.getUser().getEmail().equals(email)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (BookingStatus.CONFIRMED.equals(booking.getStatus())) {
            throw new AppException(ErrorCode.BOOKING_ALREADY_CONFIRMED);
        }

        Traveler traveler = booking.getTravelerList().stream()
                .filter(t -> t.getId().equals(travelerId))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        travelerMapper.updateTraveler(request, traveler);
        travelerRepository.save(traveler);

        return toResponseWithPrice(bookingRepository.findById(bookingId).orElseThrow(), booking.getDeparture());
    }

    @Transactional
    public BookingResponse cancelBooking(Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (!booking.getUser().getEmail().equals(email)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (!BookingStatus.PENDING.equals(booking.getStatus())) {
            throw new AppException(ErrorCode.BOOKING_CANNOT_CANCEL);
        }

        booking.setStatus(BookingStatus.CANCELLED);

        if (booking.getPaymentList() != null) {
            booking.getPaymentList().stream()
                    .filter(p -> PaymentStatus.PENDING.equals(p.getStatus()))
                    .forEach(p -> p.setStatus(PaymentStatus.FAILED));
        }

        Departure departure = booking.getDeparture();
        departure.setBookedSeats(departure.getBookedSeats() - booking.getTravelerList().size());
        if (DepartureStatus.FULL.equals(departure.getStatus())) {
            departure.setStatus(DepartureStatus.AVAILABLE);
        }

        departureRepository.save(departure);
        return toResponseWithPrice(bookingRepository.save(booking), departure);
    }

    @Transactional
    public void deleteMyBooking(Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (!booking.getUser().getEmail().equals(email)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        Departure departure = booking.getDeparture();
        departure.setBookedSeats(Math.max(0, departure.getBookedSeats() - booking.getTravelerList().size()));
        if (DepartureStatus.FULL.equals(departure.getStatus())) {
            departure.setStatus(DepartureStatus.AVAILABLE);
        }
        // Xóa khỏi collection của departure trước để tránh Hibernate re-insert do CascadeType.ALL
        if (departure.getBookingList() != null) {
            departure.getBookingList().remove(booking);
        }
        departureRepository.save(departure);

        bookingRepository.delete(booking);
    }

    // ===== BUSINESS =====

    public BookingResponse getBookingByIdForBusiness(Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (!booking.getDeparture().getTour().getUser().getEmail().equals(email)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        return toResponseWithPrice(booking, booking.getDeparture());
    }

    public PageResponse<BookingResponse> getMyToursBookings(int page, int size) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Pageable pageable = PageRequest.of(page, size);
        Page<Booking> result = bookingRepository.findByDepartureTourUserEmail(email, pageable);
        return toPageResponse(result);
    }

    public PageResponse<BookingResponse> searchMyToursBookings(String keyword, int page, int size) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Pageable pageable = PageRequest.of(page, size);
        Page<Booking> result = bookingRepository.searchByTourOwner(email, normalizeKeyword(keyword), pageable);
        return toPageResponse(result);
    }

    @Transactional
    public BookingResponse confirmBooking(Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (!booking.getDeparture().getTour().getUser().getEmail().equals(email)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        boolean hasPaidPayment = booking.getPaymentList() != null &&
                booking.getPaymentList().stream()
                        .anyMatch(p -> PaymentStatus.SUCCESS.equals(p.getStatus()));
        if (!hasPaidPayment) {
            throw new AppException(ErrorCode.BOOKING_NOT_PAID);
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        return toResponseWithPrice(bookingRepository.save(booking), booking.getDeparture());
    }

    @Transactional
    public BookingResponse rejectBooking(Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (!booking.getDeparture().getTour().getUser().getEmail().equals(email)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (booking.getDeparture().getDepartureDate().isBefore(LocalDate.now())) {
            throw new AppException(ErrorCode.DEPARTURE_ALREADY_DEPARTED);
        }

        booking.setStatus(BookingStatus.REJECTED);

        if (booking.getPaymentList() != null) {
            booking.getPaymentList().stream()
                    .filter(p -> PaymentStatus.SUCCESS.equals(p.getStatus()))
                    .forEach(p -> p.setStatus(PaymentStatus.REFUNDED));
        }

        Departure departure = booking.getDeparture();
        departure.setBookedSeats(departure.getBookedSeats() - booking.getTravelerList().size());
        if (DepartureStatus.FULL.equals(departure.getStatus())) {
            departure.setStatus(DepartureStatus.AVAILABLE);
        }
        departureRepository.save(departure);

        return toResponseWithPrice(bookingRepository.save(booking), departure);
    }

    // ===== ADMIN =====

    public BookingResponse getBookingByIdForAdmin(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
        return toResponseWithPrice(booking, booking.getDeparture());
    }

    public PageResponse<BookingResponse> getAllBookings(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Booking> result = bookingRepository.findAll(pageable);
        return toPageResponse(result);
    }

    public PageResponse<BookingResponse> searchAllBookings(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Booking> result = bookingRepository.searchAll(normalizeKeyword(keyword), pageable);
        return toPageResponse(result);
    }

    public PageResponse<BookingResponse> getBookingsByUser(Long userId, int page, int size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Pageable pageable = PageRequest.of(page, size);
        Page<Booking> result = bookingRepository.findByUserId(userId, pageable);
        return toPageResponse(result);
    }

    public PageResponse<BookingResponse> getBookingsByTour(Long tourId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Booking> result = bookingRepository.findByDepartureTourId(tourId, pageable);
        return toPageResponse(result);
    }

    public PageResponse<BookingResponse> getBookingsByDeparture(Long departureId, int page, int size) {
        departureRepository.findById(departureId)
                .orElseThrow(() -> new AppException(ErrorCode.DEPARTURE_NOT_FOUND));
        Pageable pageable = PageRequest.of(page, size);
        Page<Booking> result = bookingRepository.findByDepartureId(departureId, pageable);
        return toPageResponse(result);
    }

    @Transactional
    public BookingResponse updateBookingForAdmin(Long id, BookingUpdateRequest request) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
        bookingMapper.updateBooking(request, booking);
        return toResponseWithPrice(bookingRepository.save(booking), booking.getDeparture());
    }

    @Transactional
    public void deleteBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        Departure departure = booking.getDeparture();
        departure.setBookedSeats(Math.max(0, departure.getBookedSeats() - booking.getTravelerList().size()));
        if (DepartureStatus.FULL.equals(departure.getStatus())) {
            departure.setStatus(DepartureStatus.AVAILABLE);
        }
        // Xóa khỏi collection của departure trước để tránh Hibernate re-insert do CascadeType.ALL
        if (departure.getBookingList() != null) {
            departure.getBookingList().remove(booking);
        }
        departureRepository.save(departure);

        bookingRepository.delete(booking);
    }

    // ===== HELPER =====

    private String generateBookingCode() {
        return "BK" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) return null;
        String normalized = keyword.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private PageResponse<BookingResponse> toPageResponse(Page<Booking> page) {
        return PageResponse.<BookingResponse>builder()
                .content(page.getContent().stream()
                        .map(b -> toResponseWithPrice(b, b.getDeparture()))
                        .toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    private BookingResponse toResponseWithPrice(Booking booking, Departure departure) {
        BookingResponse bookingResponse = bookingMapper.toResponse(booking);

        List<TravelerResponse> travelerResponseList = booking.getTravelerList().stream()
                .map(t -> {
                    TravelerResponse travelerResponse = travelerMapper.toResponse(t);
                    departure.getDeparturePriceList().stream()
                            .filter(p -> p.getPassengerType().getId().equals(t.getPassengerType().getId()))
                            .findFirst()
                            .ifPresent(p -> travelerResponse.setPrice(p.getPrice()));
                    return travelerResponse;
                }).toList();

        bookingResponse.setTravelerResponseList(travelerResponseList);
        return bookingResponse;
    }
}