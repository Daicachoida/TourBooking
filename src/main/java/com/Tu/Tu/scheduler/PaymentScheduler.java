package com.Tu.Tu.scheduler;

import com.Tu.Tu.constant.BookingStatus;
import com.Tu.Tu.constant.DepartureStatus;
import com.Tu.Tu.constant.PaymentStatus;
import com.Tu.Tu.entity.Booking;
import com.Tu.Tu.entity.Departure;
import com.Tu.Tu.entity.Payment;
import com.Tu.Tu.repository.BookingRepository;
import com.Tu.Tu.repository.DepartureRepository;
import com.Tu.Tu.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentScheduler {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final DepartureRepository departureRepository;

    /**
     * Chạy mỗi 1 phút sau khi job kết thúc— tìm các payment VNPay PENDING đã quá hạn
     * → set Payment FAILED, Booking EXPIRED, nhả ghế
     */
    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void expireOverduePayments() {
        List<Payment> expiredPayments = paymentRepository
                .findExpiredPendingPayments(LocalDateTime.now());

        if (expiredPayments.isEmpty()) return;

        log.info("Tìm thấy {} payment hết hạn, đang xử lý...", expiredPayments.size());

        for (Payment payment : expiredPayments) {
            try {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);

                Booking booking = payment.getBooking();

                // Chỉ hủy booking nếu chưa có payment thành công nào khác
                boolean hasSuccessPayment = booking.getPaymentList().stream()
                        .anyMatch(p -> PaymentStatus.SUCCESS.equals(p.getStatus()));

                if (!hasSuccessPayment && BookingStatus.PENDING.equals(booking.getStatus())) {
                    booking.setStatus(BookingStatus.EXPIRED);
                    bookingRepository.save(booking);

                    // Nhả ghế
                    Departure departure = booking.getDeparture();
                    int seatsToRelease = booking.getTravelerList() != null
                            ? booking.getTravelerList().size() : 0;

                    if (seatsToRelease > 0) {
                        departure.setBookedSeats(
                                Math.max(0, departure.getBookedSeats() - seatsToRelease));
                        if (DepartureStatus.FULL.equals(departure.getStatus())) {
                            departure.setStatus(DepartureStatus.AVAILABLE);
                        }
                        departureRepository.save(departure);
                    }

                    log.info("Booking {} đã EXPIRED, nhả {} ghế",
                            booking.getBookingCode(), seatsToRelease);
                }
            } catch (Exception e) {
                log.error("Lỗi xử lý payment hết hạn id={}: {}", payment.getId(), e.getMessage());
            }
        }
    }

    /**
     * Chạy mỗi 30 phút:
     * - Booking PENDING quá 1 ngày từ createAt mà chưa có payment SUCCESS -> REJECTED
     * - Payment PENDING của booking đó -> FAILED
     * - Nhả ghế đã giữ
     */
    @Scheduled(fixedDelay = 1800000)
    @Transactional
    public void rejectOverdueUnpaidBookings() {
        LocalDate cutoffDate = LocalDate.now().minusDays(1);
        List<Booking> overdueBookings = bookingRepository.findPendingBookingsOlderThan(cutoffDate);

        if (overdueBookings.isEmpty()) return;

        log.info("Tìm thấy {} booking quá hạn 1 ngày chưa xác nhận", overdueBookings.size());

        for (Booking booking : overdueBookings) {
            try {
                boolean hasSuccessPayment = booking.getPaymentList() != null
                        && booking.getPaymentList().stream()
                        .anyMatch(p -> PaymentStatus.SUCCESS.equals(p.getStatus()));

                if (hasSuccessPayment) {
                    continue;
                }

                // Fail tất cả payment PENDING của booking
                if (booking.getPaymentList() != null) {
                    booking.getPaymentList().stream()
                            .filter(p -> PaymentStatus.PENDING.equals(p.getStatus()))
                            .forEach(p -> {
                                p.setStatus(PaymentStatus.FAILED);
                                paymentRepository.save(p);
                            });
                }

                // Reject booking
                booking.setStatus(BookingStatus.REJECTED);
                bookingRepository.save(booking);

                // Nhả ghế nếu có
                Departure departure = booking.getDeparture();
                int seatsToRelease = booking.getTravelerList() != null
                        ? booking.getTravelerList().size() : 0;

                if (seatsToRelease > 0 && departure != null) {
                    departure.setBookedSeats(Math.max(0, departure.getBookedSeats() - seatsToRelease));
                    if (DepartureStatus.FULL.equals(departure.getStatus())) {
                        departure.setStatus(DepartureStatus.AVAILABLE);
                    }
                    departureRepository.save(departure);
                }

                log.info("Booking {} quá hạn 1 ngày -> REJECTED, nhả {} ghế",
                        booking.getBookingCode(), seatsToRelease);
            } catch (Exception e) {
                log.error("Lỗi xử lý booking quá hạn id={}: {}", booking.getId(), e.getMessage());
            }
        }
    }
}