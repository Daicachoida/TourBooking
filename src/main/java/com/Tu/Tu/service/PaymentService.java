package com.Tu.Tu.service;

import com.Tu.Tu.configuration.VNPayConfig;
import com.Tu.Tu.constant.BookingStatus;
import com.Tu.Tu.constant.DepartureStatus;
import com.Tu.Tu.constant.PaymentStatus;
import com.Tu.Tu.dto.request.PaymentCreateRequest;
import com.Tu.Tu.dto.response.PageResponse;
import com.Tu.Tu.dto.response.PaymentResponse;
import com.Tu.Tu.entity.Booking;
import com.Tu.Tu.entity.Departure;
import com.Tu.Tu.entity.Payment;
import com.Tu.Tu.exception.AppException;
import com.Tu.Tu.exception.ErrorCode;
import com.Tu.Tu.mapper.PaymentMapper;
import com.Tu.Tu.repository.BookingRepository;
import com.Tu.Tu.repository.DepartureRepository;
import com.Tu.Tu.repository.PaymentRepository;
import com.Tu.Tu.repository.UserRepository;
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
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentService {

    PaymentRepository paymentRepository;
    BookingRepository bookingRepository;
    DepartureRepository departureRepository;
    UserRepository userRepository;
    PaymentMapper paymentMapper;
    VNPayService vnPayService;

    // ===== USER =====

    @Transactional
    public PaymentResponse createPayment(PaymentCreateRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (!booking.getUser().getEmail().equals(email)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (!BookingStatus.PENDING.equals(booking.getStatus())) {
            throw new AppException(ErrorCode.BOOKING_INVALID_STATUS);
        }

        boolean alreadyPaid = booking.getPaymentList() != null && booking.getPaymentList().stream()
                .anyMatch(p -> PaymentStatus.SUCCESS.equals(p.getStatus()));
        if (alreadyPaid) {
            throw new AppException(ErrorCode.PAYMENT_ALREADY_PAID);
        }

        boolean alreadyPending = booking.getPaymentList() != null && booking.getPaymentList().stream()
                .anyMatch(p -> PaymentStatus.PENDING.equals(p.getStatus()));
        if (alreadyPending) {
            throw new AppException(ErrorCode.PAYMENT_ALREADY_PENDING);
        }

        Payment payment = Payment.builder()
                .amount(booking.getTotalAmount())
                .method(request.getMethod())
                .status(PaymentStatus.PENDING)
                .booking(booking)
                .build();

        return paymentMapper.toResponse(paymentRepository.save(payment));
    }

    /**
     * Tạo payment VNPay — trả về URL redirect sang trang thanh toán VNPay
     */
    @Transactional
    public PaymentResponse createVNPayPayment(Long bookingId, String ipAddress, String returnUrl) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (!booking.getUser().getEmail().equals(email)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (!BookingStatus.PENDING.equals(booking.getStatus())) {
            throw new AppException(ErrorCode.BOOKING_INVALID_STATUS);
        }

        boolean alreadyPaid = booking.getPaymentList() != null && booking.getPaymentList().stream()
                .anyMatch(p -> PaymentStatus.SUCCESS.equals(p.getStatus()));
        if (alreadyPaid) {
            throw new AppException(ErrorCode.PAYMENT_ALREADY_PAID);
        }

        // Hủy payment PENDING cũ nếu có (user tạo lại)
        if (booking.getPaymentList() != null) {
            booking.getPaymentList().stream()
                    .filter(p -> PaymentStatus.PENDING.equals(p.getStatus()))
                    .forEach(p -> p.setStatus(PaymentStatus.FAILED));
        }

        String transactionRef = vnPayService.generateTransactionRef(bookingId);
        LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(VNPayConfig.PAYMENT_TIMEOUT_MINUTES);

        Payment payment = Payment.builder()
                .amount(booking.getTotalAmount())
                .method("VNPAY")
                .status(PaymentStatus.PENDING)
                .transactionRef(transactionRef)
                .expiredAt(expiredAt)
                .booking(booking)
                .build();

        payment = paymentRepository.save(payment);

        String orderInfo = "Thanh toan booking " + booking.getBookingCode();
        String paymentUrl = vnPayService.createPaymentUrl(
            transactionRef, booking.getTotalAmount(), orderInfo, ipAddress, returnUrl);

        PaymentResponse response = paymentMapper.toResponse(payment);
        response.setPaymentUrl(paymentUrl);
        return response;
    }

    /**
     * Xử lý webhook/return URL từ VNPay
     * VNPay gọi GET về return URL với các params xác nhận
     */
    @Transactional
    public PaymentResponse handleVNPayWebhook(Map<String, String> params) {
        // Bước 1: Verify chữ ký
        if (!vnPayService.verifyWebhook(params)) {
            throw new AppException(ErrorCode.VNPAY_INVALID_SIGNATURE);
        }

        String transactionRef = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");
        String vnpayTransactionId = params.get("vnp_TransactionNo");

        Payment payment = paymentRepository.findByTransactionRef(transactionRef)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        // Idempotency: nếu đã xử lý rồi thì bỏ qua
        if (PaymentStatus.SUCCESS.equals(payment.getStatus()) ||
                PaymentStatus.FAILED.equals(payment.getStatus())) {
            log.warn("Payment {} đã được xử lý trước đó, bỏ qua webhook", transactionRef);
            return paymentMapper.toResponse(payment);
        }

        // Bước 2: Kiểm tra mã phản hồi VNPay
        // "00" = thành công
        if ("00".equals(responseCode)) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaymentTime(LocalDate.now());
            payment.setVnpayTransactionId(vnpayTransactionId);

            // Auto confirm booking
            Booking booking = payment.getBooking();
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);

            log.info("VNPay webhook: Payment {} SUCCESS, Booking {} CONFIRMED",
                    transactionRef, booking.getBookingCode());
        } else {
            // Thanh toán thất bại → nhả ghế
            payment.setStatus(PaymentStatus.FAILED);
            payment.setVnpayTransactionId(vnpayTransactionId);

            Booking booking = payment.getBooking();
            Departure departure = booking.getDeparture();
            departure.setBookedSeats(departure.getBookedSeats() - booking.getTravelerList().size());
            if (DepartureStatus.FULL.equals(departure.getStatus())) {
                departure.setStatus(DepartureStatus.AVAILABLE);
            }
            departureRepository.save(departure);

            log.info("VNPay webhook: Payment {} FAILED (code: {}), đã nhả ghế",
                    transactionRef, responseCode);
        }

        return paymentMapper.toResponse(paymentRepository.save(payment));
    }

    public PageResponse<PaymentResponse> getMyPayments(int page, int size) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Pageable pageable = PageRequest.of(page, size);
        Page<Payment> result = paymentRepository.findByBookingUserEmail(email, pageable);
        return toPageResponse(result);
    }

    public PaymentResponse getMyPaymentById(Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
        if (!payment.getBooking().getUser().getEmail().equals(email)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return paymentMapper.toResponse(payment);
    }

    public PageResponse<PaymentResponse> getMyPaymentsByBooking(Long bookingId, int page, int size) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
        if (!booking.getUser().getEmail().equals(email)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<Payment> result = paymentRepository.findByBookingId(bookingId, pageable);
        return toPageResponse(result);
    }

    @Transactional
    public PaymentResponse cancelPayment(Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
        if (!payment.getBooking().getUser().getEmail().equals(email)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        if (!PaymentStatus.PENDING.equals(payment.getStatus())) {
            throw new AppException(ErrorCode.PAYMENT_CANNOT_CANCEL);
        }
        payment.setStatus(PaymentStatus.FAILED);
        return paymentMapper.toResponse(paymentRepository.save(payment));
    }

    // ===== BUSINESS =====

    public PageResponse<PaymentResponse> getPaymentsByMyTours(String keyword, int page, int size) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Pageable pageable = PageRequest.of(page, size);
        Page<Payment> result = paymentRepository.findByTourOwnerEmail(email, normalizeKeyword(keyword), pageable);
        return toPageResponse(result);
    }

    public PaymentResponse getPaymentByIdForBusiness(Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
        if (!payment.getBooking().getDeparture().getTour().getUser().getEmail().equals(email)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return paymentMapper.toResponse(payment);
    }

    @Transactional
    public PaymentResponse confirmPayment(Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
        if (!payment.getBooking().getDeparture().getTour().getUser().getEmail().equals(email)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        if (!PaymentStatus.PENDING.equals(payment.getStatus())) {
            throw new AppException(ErrorCode.PAYMENT_CANNOT_CONFIRM);
        }
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaymentTime(LocalDate.now());
        Booking booking = payment.getBooking();
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
        return paymentMapper.toResponse(paymentRepository.save(payment));
    }

    @Transactional
    public PaymentResponse refundPayment(Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
        if (!payment.getBooking().getDeparture().getTour().getUser().getEmail().equals(email)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        if (!PaymentStatus.SUCCESS.equals(payment.getStatus())) {
            throw new AppException(ErrorCode.PAYMENT_CANNOT_REFUND);
        }
        return processRefund(payment);
    }

    // ===== ADMIN =====

    public PageResponse<PaymentResponse> getPaymentsByUser(Long userId, int page, int size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Pageable pageable = PageRequest.of(page, size);
        Page<Payment> result = paymentRepository.findByBookingUserId(userId, pageable);
        return toPageResponse(result);
    }

    public PageResponse<PaymentResponse> getAllPayments(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Payment> result = paymentRepository.searchAll(normalizeKeyword(keyword), pageable);
        return toPageResponse(result);
    }

    public PaymentResponse getPaymentById(Long id) {
        return paymentMapper.toResponse(
                paymentRepository.findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND)));
    }

    public PageResponse<PaymentResponse> getPaymentsByBooking(Long bookingId, int page, int size) {
        bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
        Pageable pageable = PageRequest.of(page, size);
        Page<Payment> result = paymentRepository.findByBookingId(bookingId, pageable);
        return toPageResponse(result);
    }

    @Transactional
    public PaymentResponse confirmPaymentByAdmin(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
        if (!PaymentStatus.PENDING.equals(payment.getStatus())) {
            throw new AppException(ErrorCode.PAYMENT_CANNOT_CONFIRM);
        }
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaymentTime(LocalDate.now());
        Booking booking = payment.getBooking();
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
        return paymentMapper.toResponse(paymentRepository.save(payment));
    }

    @Transactional
    public PaymentResponse refundPaymentByAdmin(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
        if (!PaymentStatus.SUCCESS.equals(payment.getStatus())) {
            throw new AppException(ErrorCode.PAYMENT_CANNOT_REFUND);
        }
        return processRefund(payment);
    }

    @Transactional
    public void deletePayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
        if (PaymentStatus.SUCCESS.equals(payment.getStatus()) ||
                PaymentStatus.REFUNDED.equals(payment.getStatus())) {
            throw new AppException(ErrorCode.PAYMENT_CANNOT_DELETE);
        }
        // Xóa khỏi collection của booking trước để tránh Hibernate re-insert do CascadeType.ALL
        Booking booking = payment.getBooking();
        if (booking != null && booking.getPaymentList() != null) {
            booking.getPaymentList().remove(payment);
        }
        paymentRepository.delete(payment);
    }

    // ===== HELPER =====

    private PaymentResponse processRefund(Payment payment) {
        payment.setStatus(PaymentStatus.REFUNDED);
        Booking booking = payment.getBooking();
        booking.setStatus(BookingStatus.CANCELLED);
        Departure departure = booking.getDeparture();
        departure.setBookedSeats(departure.getBookedSeats() - booking.getTravelerList().size());
        if (DepartureStatus.FULL.equals(departure.getStatus())) {
            departure.setStatus(DepartureStatus.AVAILABLE);
        }
        departureRepository.save(departure);
        bookingRepository.save(booking);
        return paymentMapper.toResponse(paymentRepository.save(payment));
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) return null;
        String normalized = keyword.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private PageResponse<PaymentResponse> toPageResponse(Page<Payment> page) {
        return PageResponse.<PaymentResponse>builder()
                .content(page.getContent().stream().map(paymentMapper::toResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}