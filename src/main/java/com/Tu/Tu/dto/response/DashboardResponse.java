package com.Tu.Tu.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DashboardResponse {

    // Chart theo năm: key "2025" → doanh thu
    Map<String, Long> revenueByYear;

    // Chart theo tháng của năm: key "1"→"12" → doanh thu
    Map<String, Long> revenueByMonthOfYear;

    // Chart theo ngày của tháng: key "1"→"31" → doanh thu
    Map<String, Long> revenueByDayOfMonth;

    // VIP vs Thường: key "vip" / "normal" → doanh thu
    Map<String, Long> revenueVipVsNormal;

    // ===== OVERVIEW =====
    long totalTours;
    long activeTours;
    long pendingTours;
    long totalUsers;
    long totalBusinesses;
    long totalBookings;
    long pendingBookings;
    long confirmedBookings;
    long cancelledBookings;
    long rejectedBookings;
    long expiredBookings;
    long totalReviews;
    long totalRevenue;

    // ===== CHARTS =====
    // Key: "2025-01" → value: doanh thu tháng đó
    Map<String, Long> revenueByMonth;

    // Key: "2025-01" → value: số booking tháng đó
    Map<String, Long> bookingsByMonth;

    // Top tours
    List<TourStatResponse> topBookedTours;
    List<TourStatResponse> topRatedTours;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TourStatResponse {
        Long tourId;
        String tourName;
        long bookingCount;
        double averageRating;
        long revenue;
    }
}