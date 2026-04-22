package com.Tu.Tu.controller;

import com.Tu.Tu.dto.request.TourCreateRequest;
import com.Tu.Tu.dto.request.TourUpdateRequest;

import com.Tu.Tu.dto.response.ApiResponse;
import com.Tu.Tu.dto.response.PageResponse;
import com.Tu.Tu.dto.response.TourResponse;
import com.Tu.Tu.service.TourService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tours")
@RequiredArgsConstructor
@Slf4j
public class TourController {

    private final TourService tourService;

    // ===== PUBLIC =====

    @GetMapping
    ApiResponse<PageResponse<TourResponse>> getAllActiveTours(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return ApiResponse.<PageResponse<TourResponse>>builder()
                .result(tourService.getAllActiveTours(page, size))
                .message("success")
                .build();
    }

    @GetMapping("/search")
    ApiResponse<PageResponse<TourResponse>> searchTours(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String departureLocation,
            @RequestParam(required = false) Integer durationDays,
            @RequestParam(required = false) Long minPrice,
            @RequestParam(required = false) Long maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return ApiResponse.<PageResponse<TourResponse>>builder()
                .result(tourService.searchTours(keyword, departureLocation, durationDays, minPrice, maxPrice, page, size))
                .message("success")
                .build();
    }

    @GetMapping("/search/departure")
    ApiResponse<PageResponse<TourResponse>> searchToursByDepartureLocation(
            @RequestParam(required = false) String departureKeyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return ApiResponse.<PageResponse<TourResponse>>builder()
                .result(tourService.searchToursByDepartureLocation(departureKeyword, page, size))
                .message("success")
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<TourResponse> getTourById(@PathVariable Long id) {
        return ApiResponse.<TourResponse>builder()
                .result(tourService.getTourById(id))
                .message("success")
                .build();
    }

    // ===== BUSINESS ONLY =====

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_BUSINESS')")
    ApiResponse<TourResponse> createTour(@RequestBody TourCreateRequest request) {
        return ApiResponse.<TourResponse>builder()
                .result(tourService.createTour(request))
                .message("success")
                .build();
    }

    @GetMapping("/my-tours")
    @PreAuthorize("hasAuthority('ROLE_BUSINESS')")
    ApiResponse<PageResponse<TourResponse>> getMyTours(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return ApiResponse.<PageResponse<TourResponse>>builder()
                .result(tourService.getMyTours(page, size))
                .message("success")
                .build();
    }

    // ===== BUSINESS + ADMIN =====

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_BUSINESS', 'ROLE_ADMIN')")
    ApiResponse<TourResponse> updateTour(@PathVariable Long id, @RequestBody TourUpdateRequest request) {
        return ApiResponse.<TourResponse>builder()
                .result(tourService.updateTour(id, request))
                .message("success")
                .build();
    }

    /**
     * Xóa mềm (soft delete) — business và admin dùng trong vận hành thường ngày.
     * Tour chuyển sang INACTIVE, toàn bộ booking/departure/payment giữ nguyên.
     * Idempotent: gọi nhiều lần vẫn trả success.
     * Endpoint: DELETE /tours/my-tours/{id}
     */
    @DeleteMapping("/my-tours/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_BUSINESS', 'ROLE_ADMIN')")
    ApiResponse<Void> softDeleteTour(@PathVariable Long id) {
        tourService.softDeleteTour(id);
        return ApiResponse.<Void>builder().message("success").build();
    }

    // ===== ADMIN =====

    @GetMapping("/admin/all")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    ApiResponse<PageResponse<TourResponse>> getAllTours(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return ApiResponse.<PageResponse<TourResponse>>builder()
                .result(tourService.getAllTours(page, size))
                .message("success")
                .build();
    }

    /**
     * Admin search/filter tours
     * GET /tours/admin/search?keyword=...&status=pending&isApproved=false&minPrice=...&maxPrice=...
     *                        &page=0&size=10&sortBy=createAt&sortDir=desc
     *
     * Lọc pending:  ?status=pending&isApproved=false
     * Lọc active:   ?status=active&isApproved=true
     * Lọc rejected: ?status=rejected
     */
    @GetMapping("/admin/search")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    ApiResponse<PageResponse<TourResponse>> adminSearchTours(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean isApproved,
            @RequestParam(required = false) Long minPrice,
            @RequestParam(required = false) Long maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ApiResponse.<PageResponse<TourResponse>>builder()
                .result(tourService.adminSearchTours(keyword, status, isApproved, minPrice, maxPrice, page, size, sortBy, sortDir))
                .message("success")
                .build();
    }

    @PutMapping("/admin/{id}/approve")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    ApiResponse<TourResponse> approveTour(@PathVariable Long id) {
        return ApiResponse.<TourResponse>builder()
                .result(tourService.approveTour(id))
                .message("success")
                .build();
    }

    @PutMapping("/admin/{id}/reject")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    ApiResponse<TourResponse> rejectTour(@PathVariable Long id) {
        return ApiResponse.<TourResponse>builder()
                .result(tourService.rejectTour(id))
                .message("success")
                .build();
    }

    @PutMapping("/admin/{id}/restore")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    ApiResponse<TourResponse> restoreTour(@PathVariable Long id) {
        return ApiResponse.<TourResponse>builder()
                .result(tourService.restoreTour(id))
                .message("success")
                .build();
    }

    @PutMapping("/admin/{id}/deactivate")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    ApiResponse<TourResponse> deactivateTour(@PathVariable Long id) {
        return ApiResponse.<TourResponse>builder()
                .result(tourService.deactivateTour(id))
                .message("success")
                .build();
    }

    /**
     * Hard delete — CHỈ ADMIN, chỉ dùng để dọn dữ liệu rác.
     * Điều kiện cứng: không có booking, không còn departure active/future.
     * Endpoint: DELETE /tours/admin/{id}/hard
     * KHÔNG dùng chung với nút xóa thường ở FE.
     */
    @DeleteMapping("/admin/{id}/hard")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    ApiResponse<Void> hardDeleteTour(@PathVariable Long id) {
        tourService.hardDeleteTour(id);
        return ApiResponse.<Void>builder().message("success").build();
    }

    @PutMapping("/admin/{id}/vip")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    ApiResponse<TourResponse> assignVIPTour(
            @PathVariable Long id,
            @RequestParam Boolean isVIP) {
        return ApiResponse.<TourResponse>builder()
                .result(tourService.assignVIPTour(id, isVIP))
                .message("success")
                .build();
    }
}