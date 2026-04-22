package com.Tu.Tu.controller;

import com.Tu.Tu.dto.response.ApiResponse;
import com.Tu.Tu.dto.response.DashboardResponse;
import com.Tu.Tu.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final DashboardService dashboardService;

    // ===== ADMIN =====

    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<DashboardResponse> getAdminDashboard() {
        return ApiResponse.<DashboardResponse>builder()
                .result(dashboardService.getAdminDashboard())
                .message("success")
                .build();
    }

    @GetMapping("/admin/revenue/yearly")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<Map<String, Long>> getAdminRevenueByYear() {
        return ApiResponse.<Map<String, Long>>builder()
                .result(dashboardService.getAdminRevenueByYear())
                .message("success")
                .build();
    }

    @GetMapping("/admin/revenue/monthly")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<Map<String, Long>> getAdminRevenueByMonth(@RequestParam int year) {
        return ApiResponse.<Map<String, Long>>builder()
                .result(dashboardService.getAdminRevenueByMonthOfYear(year))
                .message("success")
                .build();
    }

    @GetMapping("/admin/revenue/daily")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<Map<String, Long>> getAdminRevenueByDay(
            @RequestParam int year,
            @RequestParam int month) {
        return ApiResponse.<Map<String, Long>>builder()
                .result(dashboardService.getAdminRevenueByDayOfMonth(year, month))
                .message("success")
                .build();
    }

    @GetMapping("/admin/revenue/vip")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<Map<String, Long>> getAdminRevenueVipVsNormal(@RequestParam int year) {
        return ApiResponse.<Map<String, Long>>builder()
                .result(dashboardService.getAdminRevenueVipVsNormal(year))
                .message("success")
                .build();
    }

    // ===== BUSINESS =====

    @GetMapping("/business")
    @PreAuthorize("hasAuthority('ROLE_BUSINESS')")
    public ApiResponse<DashboardResponse> getBusinessDashboard() {
        return ApiResponse.<DashboardResponse>builder()
                .result(dashboardService.getBusinessDashboard())
                .message("success")
                .build();
    }

    @GetMapping("/business/revenue/yearly")
    @PreAuthorize("hasAuthority('ROLE_BUSINESS')")
    public ApiResponse<Map<String, Long>> getBusinessRevenueByYear() {
        return ApiResponse.<Map<String, Long>>builder()
                .result(dashboardService.getBusinessRevenueByYear())
                .message("success")
                .build();
    }

    @GetMapping("/business/revenue/monthly")
    @PreAuthorize("hasAuthority('ROLE_BUSINESS')")
    public ApiResponse<Map<String, Long>> getBusinessRevenueByMonth(@RequestParam int year) {
        return ApiResponse.<Map<String, Long>>builder()
                .result(dashboardService.getBusinessRevenueByMonthOfYear(year))
                .message("success")
                .build();
    }

    @GetMapping("/business/revenue/daily")
    @PreAuthorize("hasAuthority('ROLE_BUSINESS')")
    public ApiResponse<Map<String, Long>> getBusinessRevenueByDay(
            @RequestParam int year,
            @RequestParam int month) {
        return ApiResponse.<Map<String, Long>>builder()
                .result(dashboardService.getBusinessRevenueByDayOfMonth(year, month))
                .message("success")
                .build();
    }

    @GetMapping("/business/revenue/vip")
    @PreAuthorize("hasAuthority('ROLE_BUSINESS')")
    public ApiResponse<Map<String, Long>> getBusinessRevenueVipVsNormal(@RequestParam int year) {
        return ApiResponse.<Map<String, Long>>builder()
                .result(dashboardService.getBusinessRevenueVipVsNormal(year))
                .message("success")
                .build();
    }
}