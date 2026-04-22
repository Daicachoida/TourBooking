package com.Tu.Tu.service;

import com.Tu.Tu.constant.BookingStatus;
import com.Tu.Tu.constant.PaymentStatus;
import com.Tu.Tu.constant.PredefinedRole;
import com.Tu.Tu.constant.TourStatus;
import com.Tu.Tu.dto.response.DashboardResponse;
import com.Tu.Tu.entity.Booking;
import com.Tu.Tu.entity.Payment;
import com.Tu.Tu.entity.Tour;
import com.Tu.Tu.entity.User;
import com.Tu.Tu.exception.AppException;
import com.Tu.Tu.exception.ErrorCode;
import com.Tu.Tu.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DashboardService {

    TourRepository tourRepository;
    UserRepository userRepository;
    BookingRepository bookingRepository;
    PaymentRepository paymentRepository;
    ReviewRepository reviewRepository;

    // ===== ADMIN =====

    public DashboardResponse getAdminDashboard() {
        List<Tour> allTours = tourRepository.findAll();
        List<User> allUsers = userRepository.findAll();
        List<Booking> allBookings = bookingRepository.findAll();
        List<Payment> allPayments = paymentRepository.findAll();

        long totalTours = allTours.size();
        long activeTours = allTours.stream().filter(t -> TourStatus.ACTIVE.equals(t.getStatus())).count();
        long pendingTours = allTours.stream().filter(t -> TourStatus.PENDING.equals(t.getStatus())).count();

        long totalUsers = allUsers.stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> r.getName().equals(PredefinedRole.USER_ROLE)))
                .count();
        long totalBusinesses = allUsers.stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> r.getName().equals(PredefinedRole.BUSINESS_ROLE)))
                .count();

        long totalBookings = allBookings.size();
        long pendingBookings = allBookings.stream().filter(b -> BookingStatus.PENDING.equals(b.getStatus())).count();
        long confirmedBookings = allBookings.stream().filter(b -> BookingStatus.CONFIRMED.equals(b.getStatus())).count();
        long cancelledBookings = allBookings.stream().filter(b -> BookingStatus.CANCELLED.equals(b.getStatus())).count();
        long rejectedBookings = allBookings.stream().filter(b -> BookingStatus.REJECTED.equals(b.getStatus())).count();

        long totalRevenue = allPayments.stream()
                .filter(p -> PaymentStatus.SUCCESS.equals(p.getStatus()))
                .mapToLong(Payment::getAmount)
                .sum();

        long totalReviews = reviewRepository.count();

        Map<String, Long> revenueByMonth = buildRevenueByMonth(allPayments);
        Map<String, Long> bookingsByMonth = buildBookingsByMonth(allBookings);

        List<DashboardResponse.TourStatResponse> topBookedTours = buildTopBookedTours(allTours, allBookings, allPayments, 5);
        List<DashboardResponse.TourStatResponse> topRatedTours = buildTopRatedTours(allTours, allBookings, allPayments, 5);

        return DashboardResponse.builder()
                .totalTours(totalTours)
                .activeTours(activeTours)
                .pendingTours(pendingTours)
                .totalUsers(totalUsers)
                .totalBusinesses(totalBusinesses)
                .totalBookings(totalBookings)
                .pendingBookings(pendingBookings)
                .confirmedBookings(confirmedBookings)
                .cancelledBookings(cancelledBookings)
                .rejectedBookings(rejectedBookings)
                .totalRevenue(totalRevenue)
                .totalReviews(totalReviews)
                .revenueByMonth(revenueByMonth)
                .bookingsByMonth(bookingsByMonth)
                .topBookedTours(topBookedTours)
                .topRatedTours(topRatedTours)
                .build();
    }

    // ===== BUSINESS =====

    public DashboardResponse getBusinessDashboard() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User business = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        List<Tour> myTours = tourRepository.findByUserId(business.getId());
        List<Booking> myBookings = bookingRepository.findByDepartureTourUserEmail(email);
        List<Payment> myPayments = paymentRepository.findByBookingDepartureTourUserEmail(email);

        long totalTours = myTours.size();
        long activeTours = myTours.stream().filter(t -> TourStatus.ACTIVE.equals(t.getStatus())).count();
        long pendingTours = myTours.stream().filter(t -> TourStatus.PENDING.equals(t.getStatus())).count();

        long totalBookings = myBookings.size();
        long pendingBookings = myBookings.stream().filter(b -> BookingStatus.PENDING.equals(b.getStatus())).count();
        long confirmedBookings = myBookings.stream().filter(b -> BookingStatus.CONFIRMED.equals(b.getStatus())).count();
        long cancelledBookings = myBookings.stream().filter(b -> BookingStatus.CANCELLED.equals(b.getStatus())).count();
        long rejectedBookings = myBookings.stream().filter(b -> BookingStatus.REJECTED.equals(b.getStatus())).count();
        long expiredBookings = myBookings.stream().filter(b -> BookingStatus.EXPIRED.equals(b.getStatus())).count();

        long totalRevenue = myPayments.stream()
                .filter(p -> PaymentStatus.SUCCESS.equals(p.getStatus()))
                .mapToLong(Payment::getAmount)
                .sum();

        long totalReviews = myTours.stream()
                .mapToLong(t -> t.getReviewCount())
                .sum();

        Map<String, Long> revenueByMonth = buildRevenueByMonth(myPayments);
        Map<String, Long> bookingsByMonth = buildBookingsByMonth(myBookings);

        List<DashboardResponse.TourStatResponse> topBookedTours = buildTopBookedTours(myTours, myBookings, myPayments, 5);
        List<DashboardResponse.TourStatResponse> topRatedTours = buildTopRatedTours(myTours, myBookings, myPayments, 5);

        return DashboardResponse.builder()
                .totalTours(totalTours)
                .activeTours(activeTours)
                .pendingTours(pendingTours)
                .totalBookings(totalBookings)
                .pendingBookings(pendingBookings)
                .confirmedBookings(confirmedBookings)
                .cancelledBookings(cancelledBookings)
                .rejectedBookings(rejectedBookings)
                .expiredBookings(expiredBookings)
                .totalRevenue(totalRevenue)
                .totalReviews(totalReviews)
                .revenueByMonth(revenueByMonth)
                .bookingsByMonth(bookingsByMonth)
                .topBookedTours(topBookedTours)
                .topRatedTours(topRatedTours)
                .build();
    }

    // ===== HELPER =====

    private Map<String, Long> buildRevenueByMonth(List<Payment> payments) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        Map<String, Long> result = new LinkedHashMap<>();

        LocalDate now = LocalDate.now();
        for (int i = 11; i >= 0; i--) {
            result.put(now.minusMonths(i).format(formatter), 0L);
        }

        payments.stream()
                .filter(p -> PaymentStatus.SUCCESS.equals(p.getStatus()) && p.getPaymentTime() != null)
                .forEach(p -> {
                    String key = p.getPaymentTime().format(formatter);
                    if (result.containsKey(key)) {
                        result.merge(key, p.getAmount(), Long::sum);
                    }
                });

        return result;
    }

    private Map<String, Long> buildBookingsByMonth(List<Booking> bookings) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        Map<String, Long> result = new LinkedHashMap<>();

        LocalDate now = LocalDate.now();
        for (int i = 11; i >= 0; i--) {
            result.put(now.minusMonths(i).format(formatter), 0L);
        }

        bookings.stream()
                .filter(b -> b.getCreateAt() != null)
                .forEach(b -> {
                    String key = b.getCreateAt().format(formatter);
                    if (result.containsKey(key)) {
                        result.merge(key, 1L, Long::sum);
                    }
                });

        return result;
    }

    private List<DashboardResponse.TourStatResponse> buildTopBookedTours(
            List<Tour> tours, List<Booking> bookings, List<Payment> payments, int limit) {

        return tours.stream()
                .map(tour -> {
                    long bookingCount = bookings.stream()
                            .filter(b -> b.getDeparture().getTour().getId().equals(tour.getId()))
                            .count();

                    long revenue = payments.stream()
                            .filter(p -> PaymentStatus.SUCCESS.equals(p.getStatus()))
                            .filter(p -> p.getBooking().getDeparture().getTour().getId().equals(tour.getId()))
                            .mapToLong(Payment::getAmount)
                            .sum();

                    return DashboardResponse.TourStatResponse.builder()
                            .tourId(tour.getId())
                            .tourName(tour.getName())
                            .bookingCount(bookingCount)
                            .averageRating(tour.getAverageRating() != null ? tour.getAverageRating() : 0)
                            .revenue(revenue)
                            .build();
                })
                .sorted(Comparator.comparingLong(DashboardResponse.TourStatResponse::getBookingCount).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    private List<DashboardResponse.TourStatResponse> buildTopRatedTours(
            List<Tour> tours, List<Booking> bookings, List<Payment> payments, int limit) {

        return tours.stream()
                .filter(t -> t.getReviewCount() > 0)
                .map(tour -> {
                    long bookingCount = bookings.stream()
                            .filter(b -> b.getDeparture().getTour().getId().equals(tour.getId()))
                            .count();

                    long revenue = payments.stream()
                            .filter(p -> PaymentStatus.SUCCESS.equals(p.getStatus()))
                            .filter(p -> p.getBooking().getDeparture().getTour().getId().equals(tour.getId()))
                            .mapToLong(Payment::getAmount)
                            .sum();

                    return DashboardResponse.TourStatResponse.builder()
                            .tourId(tour.getId())
                            .tourName(tour.getName())
                            .bookingCount(bookingCount)
                            .averageRating(tour.getAverageRating() != null ? tour.getAverageRating() : 0)
                            .revenue(revenue)
                            .build();
                })
                .sorted(Comparator.comparingDouble(DashboardResponse.TourStatResponse::getAverageRating).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }


    public Map<String, Long> getAdminRevenueByYear() {
        int currentYear = LocalDate.now().getYear();
        Map<String, Long> result = new LinkedHashMap<>();

        for (int y = 2025; y <= currentYear; y++) {
            result.put(String.valueOf(y), 0L);
        }

        List<Object[]> rows = paymentRepository.revenueByYear(2025);
        for (Object[] row : rows) {
            String year = String.valueOf(((Number) row[0]).intValue());
            Long total  = ((Number) row[1]).longValue();
            result.put(year, total);
        }

        return result;
    }

    public Map<String, Long> getAdminRevenueByMonthOfYear(int year) {
        Map<String, Long> result = new LinkedHashMap<>();

        for (int m = 1; m <= 12; m++) {
            result.put(String.valueOf(m), 0L);
        }

        List<Object[]> rows = paymentRepository.revenueByMonthOfYear(year);
        for (Object[] row : rows) {
            String month = String.valueOf(((Number) row[0]).intValue());
            Long total   = ((Number) row[1]).longValue();
            result.put(month, total);
        }

        return result;
    }

    public Map<String, Long> getAdminRevenueByDayOfMonth(int year, int month) {
        int daysInMonth = YearMonth.of(year, month).lengthOfMonth();
        Map<String, Long> result = new LinkedHashMap<>();

        for (int d = 1; d <= daysInMonth; d++) {
            result.put(String.valueOf(d), 0L);
        }

        List<Object[]> rows = paymentRepository.revenueByDayOfMonth(year, month);
        for (Object[] row : rows) {
            String day = String.valueOf(((Number) row[0]).intValue());
            Long total = ((Number) row[1]).longValue();
            result.put(day, total);
        }

        return result;
    }

    public Map<String, Long> getAdminRevenueVipVsNormal(int year) {
        Map<String, Long> result = new LinkedHashMap<>();

        result.put("vip", 0L);
        result.put("normal", 0L);

        List<Object[]> rows = paymentRepository.revenueVipVsNormalByYear(year);
        for (Object[] row : rows) {
            boolean isVip = toBooleanValue(row[0]);
            Long total = ((Number) row[1]).longValue();
            result.put(isVip ? "vip" : "normal", total);
        }

        return result;
    }


    public Map<String, Long> getBusinessRevenueByYear() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        int currentYear = LocalDate.now().getYear();
        Map<String, Long> result = new LinkedHashMap<>();

        for (int y = 2025; y <= currentYear; y++) {
            result.put(String.valueOf(y), 0L);
        }

        List<Object[]> rows = paymentRepository.revenueByYearAndOwner(2025, email);
        for (Object[] row : rows) {
            String year = String.valueOf(((Number) row[0]).intValue());
            Long total  = ((Number) row[1]).longValue();
            result.put(year, total);
        }

        return result;
    }

    public Map<String, Long> getBusinessRevenueByMonthOfYear(int year) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Map<String, Long> result = new LinkedHashMap<>();

        for (int m = 1; m <= 12; m++) {
            result.put(String.valueOf(m), 0L);
        }

        List<Object[]> rows = paymentRepository.revenueByMonthOfYearAndOwner(year, email);
        for (Object[] row : rows) {
            String month = String.valueOf(((Number) row[0]).intValue());
            Long total   = ((Number) row[1]).longValue();
            result.put(month, total);
        }

        return result;
    }

    public Map<String, Long> getBusinessRevenueByDayOfMonth(int year, int month) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        int daysInMonth = YearMonth.of(year, month).lengthOfMonth();
        Map<String, Long> result = new LinkedHashMap<>();

        for (int d = 1; d <= daysInMonth; d++) {
            result.put(String.valueOf(d), 0L);
        }

        List<Object[]> rows = paymentRepository.revenueByDayOfMonthAndOwner(year, month, email);
        for (Object[] row : rows) {
            String day = String.valueOf(((Number) row[0]).intValue());
            Long total = ((Number) row[1]).longValue();
            result.put(day, total);
        }

        return result;
    }

    public Map<String, Long> getBusinessRevenueVipVsNormal(int year) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Map<String, Long> result = new LinkedHashMap<>();

        result.put("vip", 0L);
        result.put("normal", 0L);

        List<Object[]> rows = paymentRepository.revenueVipVsNormalByYearAndOwner(year, email);
        for (Object[] row : rows) {
            boolean isVip = toBooleanValue(row[0]);
            Long total = ((Number) row[1]).longValue();
            result.put(isVip ? "vip" : "normal", total);
        }

        return result;
    }

    private boolean toBooleanValue(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        if (value instanceof Number numberValue) {
            return numberValue.intValue() != 0;
        }
        String text = String.valueOf(value).trim();
        return "1".equals(text) || Boolean.parseBoolean(text);
    }
}