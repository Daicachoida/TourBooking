package com.Tu.Tu.service;

import com.Tu.Tu.constant.BookingStatus;
import com.Tu.Tu.constant.DepartureStatus;
import com.Tu.Tu.constant.PaymentStatus;
import com.Tu.Tu.dto.request.DepartureCreateRequest;
import com.Tu.Tu.dto.request.DeparturePriceCreateRequest;
import com.Tu.Tu.dto.request.DeparturePriceUpdateRequest;
import com.Tu.Tu.dto.request.DepartureUpdateRequest;
import com.Tu.Tu.dto.response.DeparturePriceResponse;
import com.Tu.Tu.dto.response.DepartureResponse;
import com.Tu.Tu.dto.response.PageResponse;
import com.Tu.Tu.entity.Booking;
import com.Tu.Tu.entity.Departure;
import com.Tu.Tu.entity.DeparturePrice;
import com.Tu.Tu.entity.PassengerType;
import com.Tu.Tu.entity.Tour;
import com.Tu.Tu.exception.AppException;
import com.Tu.Tu.exception.ErrorCode;
import com.Tu.Tu.mapper.DepartureMapper;
import com.Tu.Tu.repository.BookingRepository;
import com.Tu.Tu.repository.DeparturePriceRepository;
import com.Tu.Tu.repository.DepartureRepository;
import com.Tu.Tu.repository.PassengerTypeRepository;
import com.Tu.Tu.repository.TourRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DepartureService {

    DepartureMapper departureMapper;
    DepartureRepository departureRepository;
    DeparturePriceRepository departurePriceRepository;
    PassengerTypeRepository passengerTypeRepository;
    TourRepository tourRepository;
    BookingRepository bookingRepository;

    // ===== PUBLIC =====


    public PageResponse<DepartureResponse> getDeparturesByTour(Long tourId, int page, int size) {
        tourRepository.findById(tourId)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));
        Pageable pageable = PageRequest.of(page, size);

        Page<Departure> result = departureRepository
                .findByTourIdAndStatusAndDepartureDateGreaterThanEqual(
                        tourId,
                        DepartureStatus.AVAILABLE,
                        LocalDate.now(),
                        pageable
                );

        return toPageResponse(result);
    }

    public DepartureResponse getDepartureById(Long id) {
        return departureMapper.toResponse(departureRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DEPARTURE_NOT_FOUND)));
    }

    // ===== BUSINESS =====

    /**
     * Lấy danh sách departures của tour mình sở hữu với filter
     * tourStatus: active, pending, rejected (filter theo tour)
     * departureStatus: available, full, cancelled, departed (filter theo departure)
     */
    public PageResponse<DepartureResponse> getMyDepartures(String tourStatus, String departureStatus,
                                                           LocalDate dateFrom, LocalDate dateTo,
                                                           int page, int size) {
        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Pageable pageable = PageRequest.of(page, size);
        Page<Departure> result = departureRepository.findByBusinessEmail(
                email, tourStatus, departureStatus, dateFrom, dateTo, pageable);
        return toPageResponse(result);
    }

    public DepartureResponse createDeparture(DepartureCreateRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Tour tour = tourRepository.findById(request.getTourId())
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        if (!tour.getUser().getEmail().equals(email)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        Departure departure = Departure.builder()
                .tour(tour)
                .departureDate(request.getDepartureDate())
                .capacity(request.getCapacity())
                .bookedSeats(0)
                .status(DepartureStatus.AVAILABLE)
                .build();

        return departureMapper.toResponse(departureRepository.save(departure));
    }

    public DeparturePriceResponse createDeparturePrice(DeparturePriceCreateRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Departure departure = departureRepository.findById(request.getDepartureId())
                .orElseThrow(() -> new AppException(ErrorCode.DEPARTURE_NOT_FOUND));

        if (!departure.getTour().getUser().getEmail().equals(email)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        PassengerType passengerType = passengerTypeRepository.findById(request.getPassengerTypeId())
                .orElseThrow(() -> new AppException(ErrorCode.PASSENGER_TYPE_NOT_FOUND));

        DeparturePrice departurePrice = DeparturePrice.builder()
                .departure(departure)
                .passengerType(passengerType)
                .price(request.getPrice())
                .build();

        return departureMapper.toPriceResponse(departurePriceRepository.save(departurePrice));
    }

    // ===== BUSINESS + ADMIN =====

    /**
     * Update thông tin departure
     * Rule transition:
     * - departed → không cho đổi ngày/capacity/status quay ngược
     * - cancelled → không cho đổi sang available/full
     * - cancelled → không cho đổi ngày/capacity
     */
    public DepartureResponse updateDeparture(Long id, DepartureUpdateRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isAdmin = isAdmin();

        Departure departure = departureRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DEPARTURE_NOT_FOUND));

        if (!isAdmin && !departure.getTour().getUser().getEmail().equals(email)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        String currentStatus = departure.getStatus();

        // departed: không cho sửa gì
        if (DepartureStatus.DEPARTED.equals(currentStatus)) {
            throw new AppException(ErrorCode.DEPARTURE_ALREADY_STARTED);
        }

        // cancelled: không cho reopen sang available/full
        if (DepartureStatus.CANCELLED.equals(currentStatus)) {
            if (DepartureStatus.AVAILABLE.equals(request.getStatus()) ||
                    DepartureStatus.FULL.equals(request.getStatus())) {
                throw new AppException(ErrorCode.DEPARTURE_INVALID_STATUS_TRANSITION);
            }
        }

        // Không cho chuyển về available/full từ departed (phòng ngừa)
        if (DepartureStatus.DEPARTED.equals(request.getStatus()) &&
                !DepartureStatus.DEPARTED.equals(currentStatus)) {
            throw new AppException(ErrorCode.DEPARTURE_INVALID_STATUS_TRANSITION);
        }

        // Không cho đổi ngày nếu đã có booking
        if (!departure.getBookingList().isEmpty() &&
                !request.getDepartureDate().equals(departure.getDepartureDate())) {
            throw new AppException(ErrorCode.DEPARTURE_ALREADY_BOOKED);
        }

        // Không cho giảm capacity xuống dưới bookedSeats
        if (request.getCapacity() < departure.getBookedSeats()) {
            throw new AppException(ErrorCode.CAPACITY_LESS_THAN_BOOKED);
        }

        // Chuyển sang cancelled: kiểm tra booking
        if (DepartureStatus.CANCELLED.equals(request.getStatus()) &&
                !DepartureStatus.CANCELLED.equals(currentStatus)) {
            validateCancelDeparture(departure);
        }

        departure.setDepartureDate(request.getDepartureDate());
        departure.setCapacity(request.getCapacity());
        departure.setStatus(request.getStatus());

        // Tự động cập nhật status theo bookedSeats sau khi update
        recalculateStatus(departure);

        return departureMapper.toResponse(departureRepository.save(departure));
    }

    /**
     * HARD DELETE: chỉ cho khi:
     * 1. departureDate > hôm nay
     * 2. không có booking nào
     * 3. status không phải departed
     */
    @Transactional
    public void deleteDeparture(Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isAdmin = isAdmin();

        Departure departure = departureRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DEPARTURE_NOT_FOUND));

        if (!isAdmin && !departure.getTour().getUser().getEmail().equals(email)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Không xóa nếu đã departed
        if (DepartureStatus.DEPARTED.equals(departure.getStatus())) {
            throw new AppException(ErrorCode.DEPARTURE_ALREADY_STARTED);
        }

        // Không xóa nếu ngày đã qua hoặc là hôm nay
        if (!departure.getDepartureDate().isAfter(LocalDate.now())) {
            throw new AppException(ErrorCode.DEPARTURE_ALREADY_STARTED);
        }

        // Không xóa nếu có bất kỳ booking nào (dù status gì)
        if (departure.getBookingList() != null && !departure.getBookingList().isEmpty()) {
            throw new AppException(ErrorCode.DEPARTURE_CANNOT_DELETE_WITH_BOOKINGS);
        }

        // Xóa khỏi collection của tour trước để tránh Hibernate re-insert do CascadeType.ALL
        Tour tour = departure.getTour();
        if (tour != null && tour.getDepartureList() != null) {
            tour.getDepartureList().remove(departure);
        }

        departureRepository.delete(departure);
    }

    /**
     * CANCEL DEPARTURE: update status = cancelled, không xóa vật lý
     * - Có confirmed booking hoặc payment success: cấm cancel
     * - Chỉ có pending/expired/failed booking: tự động cancel/expire booking đó + nhả ghế
     */
    @Transactional
    public DepartureResponse cancelDeparture(Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isAdmin = isAdmin();

        Departure departure = departureRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DEPARTURE_NOT_FOUND));

        if (!isAdmin && !departure.getTour().getUser().getEmail().equals(email)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (DepartureStatus.CANCELLED.equals(departure.getStatus())) {
            throw new AppException(ErrorCode.DEPARTURE_ALREADY_CANCELLED);
        }

        if (DepartureStatus.DEPARTED.equals(departure.getStatus())) {
            throw new AppException(ErrorCode.DEPARTURE_ALREADY_STARTED);
        }

        // Kiểm tra và xử lý bookings
        validateCancelDeparture(departure);
        cancelPendingBookings(departure);

        departure.setStatus(DepartureStatus.CANCELLED);
        departure.setBookedSeats(0);

        return departureMapper.toResponse(departureRepository.save(departure));
    }

    public DeparturePriceResponse updateDeparturePrice(Long id, DeparturePriceUpdateRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isAdmin = isAdmin();

        DeparturePrice departurePrice = departurePriceRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DEPARTURE_PRICE_NOT_FOUND));

        if (!isAdmin && !departurePrice.getDeparture().getTour().getUser().getEmail().equals(email)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Không sửa price nếu departure đã departed hoặc cancelled
        String status = departurePrice.getDeparture().getStatus();
        if (DepartureStatus.DEPARTED.equals(status)) {
            throw new AppException(ErrorCode.DEPARTURE_ALREADY_STARTED);
        }

        if (departurePrice.getDeparture().getBookingList() != null
                && !departurePrice.getDeparture().getBookingList().isEmpty()) {
            throw new AppException(ErrorCode.DEPARTURE_HAS_BOOKING);
        }

        departurePrice.setPrice(request.getPrice());
        return departureMapper.toPriceResponse(departurePriceRepository.save(departurePrice));
    }

    public void deleteDeparturePrice(Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isAdmin = isAdmin();

        DeparturePrice departurePrice = departurePriceRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DEPARTURE_PRICE_NOT_FOUND));

        if (!isAdmin && !departurePrice.getDeparture().getTour().getUser().getEmail().equals(email)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (DepartureStatus.DEPARTED.equals(departurePrice.getDeparture().getStatus())) {
            throw new AppException(ErrorCode.DEPARTURE_ALREADY_STARTED);
        }

        if (departurePrice.getDeparture().getBookingList() != null
                && !departurePrice.getDeparture().getBookingList().isEmpty()) {
            throw new AppException(ErrorCode.DEPARTURE_HAS_BOOKING);
        }

        departurePriceRepository.deleteById(id);
    }

    // ===== ADMIN =====

    public PageResponse<DepartureResponse> getAllDepartures(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Departure> result = departureRepository.findAll(pageable);
        return toPageResponse(result);
    }

    public PageResponse<DepartureResponse> adminSearchDepartures(String keyword, String status,
                                                                 LocalDate dateFrom, LocalDate dateTo,
                                                                 int page, int size) {
        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<Departure> result = departureRepository.adminSearch(normalizeKeyword(keyword), status, dateFrom, dateTo, pageable);
        return toPageResponse(result);
    }

    public List<DeparturePriceResponse> getAllDeparturePrices() {
        return departurePriceRepository.findAll()
                .stream().map(departureMapper::toPriceResponse).toList();
    }

    public List<DeparturePriceResponse> getDeparturePricesByDeparture(Long departureId) {
        departureRepository.findById(departureId)
                .orElseThrow(() -> new AppException(ErrorCode.DEPARTURE_NOT_FOUND));
        return departurePriceRepository.findByDepartureId(departureId)
                .stream().map(departureMapper::toPriceResponse).toList();
    }

    // ===== HELPER =====

    /**
     * Validate trước khi cancel departure:
     * - Nếu có confirmed booking hoặc payment success → cấm
     */
    private void validateCancelDeparture(Departure departure) {
        if (departure.getBookingList() == null || departure.getBookingList().isEmpty()) return;

        boolean hasConfirmedOrPaid = departure.getBookingList().stream()
                .anyMatch(b -> BookingStatus.CONFIRMED.equals(b.getStatus()) ||
                        (b.getPaymentList() != null && b.getPaymentList().stream()
                                .anyMatch(p -> PaymentStatus.SUCCESS.equals(p.getStatus()))));

        if (hasConfirmedOrPaid) {
            throw new AppException(ErrorCode.DEPARTURE_HAS_CONFIRMED_BOOKING);
        }
    }

    /**
     * Hủy tất cả booking pending/expired → cancelled, nhả ghế
     */
    private void cancelPendingBookings(Departure departure) {
        if (departure.getBookingList() == null) return;

        for (Booking booking : departure.getBookingList()) {
            if (BookingStatus.PENDING.equals(booking.getStatus()) ||
                    BookingStatus.EXPIRED.equals(booking.getStatus())) {
                booking.setStatus(BookingStatus.CANCELLED);
                bookingRepository.save(booking);
                log.info("Booking {} cancelled do departure bị hủy", booking.getBookingCode());
            }
        }
    }

    /**
     * Tự động tính lại status theo bookedSeats
     * Chỉ áp dụng khi status đang là available/full (không ghi đè cancelled/departed)
     */
    private void recalculateStatus(Departure departure) {
        String status = departure.getStatus();
        if (DepartureStatus.CANCELLED.equals(status) || DepartureStatus.DEPARTED.equals(status)) return;

        if (departure.getBookedSeats() >= departure.getCapacity()) {
            departure.setStatus(DepartureStatus.FULL);
        } else {
            departure.setStatus(DepartureStatus.AVAILABLE);
        }
    }

    private boolean isAdmin() {
        return SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) return null;
        String normalized = keyword.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private PageResponse<DepartureResponse> toPageResponse(Page<Departure> page) {
        return PageResponse.<DepartureResponse>builder()
                .content(page.getContent().stream().map(departureMapper::toResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}