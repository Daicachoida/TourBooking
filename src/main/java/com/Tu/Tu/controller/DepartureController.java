package com.Tu.Tu.controller;

import com.Tu.Tu.dto.request.DepartureCreateRequest;
import com.Tu.Tu.dto.request.DeparturePriceCreateRequest;
import com.Tu.Tu.dto.request.DeparturePriceUpdateRequest;
import com.Tu.Tu.dto.request.DepartureUpdateRequest;
import com.Tu.Tu.dto.response.ApiResponse;
import com.Tu.Tu.dto.response.DeparturePriceResponse;
import com.Tu.Tu.dto.response.DepartureResponse;
import com.Tu.Tu.dto.response.PageResponse;
import com.Tu.Tu.service.DepartureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/departures")
@RequiredArgsConstructor
@Slf4j
public class DepartureController {

    private final DepartureService departureService;

    // ===== PUBLIC =====

    @GetMapping("/tour/{tourId}")
    ApiResponse<PageResponse<DepartureResponse>> getDeparturesByTour(
            @PathVariable Long tourId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<DepartureResponse>>builder()
                .result(departureService.getDeparturesByTour(tourId, page, size))
                .message("success")
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<DepartureResponse> getDepartureById(@PathVariable Long id) {
        return ApiResponse.<DepartureResponse>builder()
                .result(departureService.getDepartureById(id))
                .message("success")
                .build();
    }

    // ===== BUSINESS ONLY =====

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_BUSINESS')")
    ApiResponse<DepartureResponse> createDeparture(@RequestBody @Valid DepartureCreateRequest request) {
        return ApiResponse.<DepartureResponse>builder()
                .result(departureService.createDeparture(request))
                .message("success")
                .build();
    }

    @PostMapping("/prices")
    @PreAuthorize("hasAuthority('ROLE_BUSINESS')")
    ApiResponse<DeparturePriceResponse> createDeparturePrice(@RequestBody @Valid DeparturePriceCreateRequest request) {
        return ApiResponse.<DeparturePriceResponse>builder()
                .result(departureService.createDeparturePrice(request))
                .message("success")
                .build();
    }

    /**
     * Business: lấy departures của tour mình với filter
     * GET /departures/business?tourStatus=active&departureStatus=available&dateFrom=...&dateTo=...
     *
     * tourStatus: active | pending | rejected
     * departureStatus: available | full | cancelled | departed
     *
     * FE dùng endpoint này để:
     * - Hiển thị danh sách lịch khởi hành
     * - Ẩn/hiện nút "Xóa" hay "Hủy lịch" dựa trên departureStatus trả về
     *   + status=available/full + departureDate > hôm nay + không có booking → hiện nút Xóa
     *   + status=available/full + có booking pending → hiện nút "Hủy lịch"
     *   + status=departed/cancelled → ẩn cả 2 nút
     */
    @GetMapping("/business")
    @PreAuthorize("hasAuthority('ROLE_BUSINESS')")
    ApiResponse<PageResponse<DepartureResponse>> getMyDepartures(
            @RequestParam(required = false) String tourStatus,
            @RequestParam(required = false) String departureStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<DepartureResponse>>builder()
                .result(departureService.getMyDepartures(tourStatus, departureStatus, dateFrom, dateTo, page, size))
                .message("success")
                .build();
    }

    // ===== BUSINESS + ADMIN =====

    /**
     * Update thông tin departure
     * Rule:
     * - departed → không cho sửa gì (400 DEPARTURE_ALREADY_STARTED)
     * - cancelled → không cho reopen sang available/full (400 DEPARTURE_INVALID_STATUS_TRANSITION)
     * - Có booking → không cho đổi ngày (400 DEPARTURE_ALREADY_BOOKED)
     * - capacity < bookedSeats → (400 CAPACITY_LESS_THAN_BOOKED)
     * - Chuyển sang cancelled khi có confirmed booking → (400 DEPARTURE_HAS_CONFIRMED_BOOKING)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_BUSINESS', 'ROLE_ADMIN')")
    ApiResponse<DepartureResponse> updateDeparture(@PathVariable Long id,
                                                   @RequestBody @Valid DepartureUpdateRequest request) {
        return ApiResponse.<DepartureResponse>builder()
                .result(departureService.updateDeparture(id, request))
                .message("success")
                .build();
    }

    /**
     * HARD DELETE departure — chỉ cho khi:
     * 1. departureDate > hôm nay
     * 2. Không có booking nào (dù status gì)
     * 3. status không phải departed/cancelled
     *
     * FE nên ẩn nút này nếu không đủ điều kiện, thay bằng nút "Hủy lịch"
     * Errors:
     * - 400 DEPARTURE_ALREADY_STARTED: đã departed hoặc ngày đã qua
     * - 400 DEPARTURE_ALREADY_CANCELLED: đã cancelled
     * - 400 DEPARTURE_CANNOT_DELETE_WITH_BOOKINGS: có booking → dùng cancel
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_BUSINESS', 'ROLE_ADMIN')")
    ApiResponse<Void> deleteDeparture(@PathVariable Long id) {
        departureService.deleteDeparture(id);
        return ApiResponse.<Void>builder().message("Departure deleted successfully").build();
    }

    /**
     * CANCEL departure — không xóa vật lý, chuyển status = cancelled
     * Dùng thay cho delete khi departure đã có booking pending
     * Tự động cancel tất cả booking pending + nhả ghế
     *
     * Cấm cancel nếu có confirmed booking hoặc payment success
     * → phải refund trước
     * Errors:
     * - 400 DEPARTURE_HAS_CONFIRMED_BOOKING: có confirmed booking
     * - 400 DEPARTURE_ALREADY_CANCELLED: đã cancelled
     * - 400 DEPARTURE_ALREADY_STARTED: đã departed
     */
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('ROLE_BUSINESS', 'ROLE_ADMIN')")
    ApiResponse<DepartureResponse> cancelDeparture(@PathVariable Long id) {
        return ApiResponse.<DepartureResponse>builder()
                .result(departureService.cancelDeparture(id))
                .message("Departure cancelled successfully")
                .build();
    }

    @PutMapping("/prices/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_BUSINESS', 'ROLE_ADMIN')")
    ApiResponse<DeparturePriceResponse> updateDeparturePrice(@PathVariable Long id,
                                                             @RequestBody @Valid DeparturePriceUpdateRequest request) {
        return ApiResponse.<DeparturePriceResponse>builder()
                .result(departureService.updateDeparturePrice(id, request))
                .message("success")
                .build();
    }

    @DeleteMapping("/prices/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_BUSINESS', 'ROLE_ADMIN')")
    ApiResponse<Void> deleteDeparturePrice(@PathVariable Long id) {
        departureService.deleteDeparturePrice(id);
        return ApiResponse.<Void>builder().message("success").build();
    }

    // ===== ADMIN =====

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    ApiResponse<PageResponse<DepartureResponse>> getAllDepartures(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<DepartureResponse>>builder()
                .result(departureService.getAllDepartures(page, size))
                .message("success")
                .build();
    }

    @GetMapping("/admin/search")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    ApiResponse<PageResponse<DepartureResponse>> adminSearchDepartures(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<DepartureResponse>>builder()
                .result(departureService.adminSearchDepartures(keyword, status, dateFrom, dateTo, page, size))
                .message("success")
                .build();
    }

    @GetMapping("/prices")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    ApiResponse<List<DeparturePriceResponse>> getAllDeparturePrices() {
        return ApiResponse.<List<DeparturePriceResponse>>builder()
                .result(departureService.getAllDeparturePrices())
                .message("success")
                .build();
    }

    @GetMapping("/prices/departure/{departureId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    ApiResponse<List<DeparturePriceResponse>> getDeparturePricesByDeparture(@PathVariable Long departureId) {
        return ApiResponse.<List<DeparturePriceResponse>>builder()
                .result(departureService.getDeparturePricesByDeparture(departureId))
                .message("success")
                .build();
    }
}