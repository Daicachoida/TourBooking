package com.Tu.Tu.controller;

import com.Tu.Tu.dto.request.BookingCreateRequest;
import com.Tu.Tu.dto.request.BookingUpdateRequest;
import com.Tu.Tu.dto.request.TravelerUpdateRequest;
import com.Tu.Tu.dto.response.ApiResponse;
import com.Tu.Tu.dto.response.BookingResponse;
import com.Tu.Tu.dto.response.PageResponse;
import com.Tu.Tu.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final BookingService bookingService;

    // ===== CUSTOMER =====

    @PostMapping
        @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_BUSINESS', 'ROLE_ADMIN')")
    ApiResponse<BookingResponse> createBooking(@RequestBody @Valid BookingCreateRequest request) {
        return ApiResponse.<BookingResponse>builder()
                .result(bookingService.createBooking(request))
                .message("success")
                .build();
    }

    @GetMapping("/my-bookings")
        @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_BUSINESS', 'ROLE_ADMIN')")
    ApiResponse<PageResponse<BookingResponse>> getMyBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<BookingResponse>>builder()
                .result(bookingService.getMyBookings(page, size))
                .message("success")
                .build();
    }

    /**
     * User search my bookings
     * GET /bookings/my-bookings/search?keyword=HaNoi&status=PENDING&fromDate=2025-01-01&toDate=2025-12-31
     */

    @GetMapping("/my-bookings/search")
        @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_BUSINESS', 'ROLE_ADMIN')")
    ApiResponse<PageResponse<BookingResponse>> searchMyBookings(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<BookingResponse>>builder()
                .result(bookingService.searchMyBookings(keyword, status, fromDate, toDate, page, size))
                .message("success")
                .build();
    }

    @GetMapping("/my-bookings/{id}")
        @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_BUSINESS', 'ROLE_ADMIN')")
    ApiResponse<BookingResponse> getMyBookingById(@PathVariable Long id) {
        return ApiResponse.<BookingResponse>builder()
                .result(bookingService.getMyBookingById(id))
                .message("success")
                .build();
    }

    @PutMapping("/my-bookings/{id}")
        @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_BUSINESS', 'ROLE_ADMIN')")
    ApiResponse<BookingResponse> updateBooking(@PathVariable Long id,
                                               @RequestBody @Valid BookingUpdateRequest request) {
        return ApiResponse.<BookingResponse>builder()
                .result(bookingService.updateBooking(id, request))
                .message("success")
                .build();
    }

    @PutMapping("/my-bookings/{bookingId}/travelers/{travelerId}")
        @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_BUSINESS', 'ROLE_ADMIN')")
    ApiResponse<BookingResponse> updateTraveler(@PathVariable Long bookingId,
                                                @PathVariable Long travelerId,
                                                @RequestBody @Valid TravelerUpdateRequest request) {
        return ApiResponse.<BookingResponse>builder()
                .result(bookingService.updateTraveler(bookingId, travelerId, request))
                .message("success")
                .build();
    }

    @PutMapping("/my-bookings/{id}/cancel")
        @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_BUSINESS', 'ROLE_ADMIN')")
    ApiResponse<BookingResponse> cancelBooking(@PathVariable Long id) {
        return ApiResponse.<BookingResponse>builder()
                .result(bookingService.cancelBooking(id))
                .message("success")
                .build();
    }

    @DeleteMapping("/my-bookings/{id}")
        @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_BUSINESS', 'ROLE_ADMIN')")
    ApiResponse<Void> deleteMyBooking(@PathVariable Long id) {
        bookingService.deleteMyBooking(id);
        return ApiResponse.<Void>builder()
                .message("success")
                .build();
    }

    // ===== BUSINESS =====

    @GetMapping("/my-tours-bookings")
    @PreAuthorize("hasAuthority('ROLE_BUSINESS')")
    ApiResponse<PageResponse<BookingResponse>> getMyToursBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<BookingResponse>>builder()
                .result(bookingService.getMyToursBookings(page, size))
                .message("success")
                .build();
    }

    @GetMapping("/my-tours-bookings/search")
    @PreAuthorize("hasAuthority('ROLE_BUSINESS')")
    ApiResponse<PageResponse<BookingResponse>> searchMyToursBookings(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<BookingResponse>>builder()
                .result(bookingService.searchMyToursBookings(keyword, page, size))
                .message("success")
                .build();
    }

    @GetMapping("/my-tours-bookings/{id}")
    @PreAuthorize("hasAuthority('ROLE_BUSINESS')")
    ApiResponse<BookingResponse> getBookingByIdForBusiness(@PathVariable Long id) {
        return ApiResponse.<BookingResponse>builder()
                .result(bookingService.getBookingByIdForBusiness(id))
                .message("success")
                .build();
    }

    @PutMapping("/my-tours-bookings/{id}/confirm")
    @PreAuthorize("hasAuthority('ROLE_BUSINESS')")
    ApiResponse<BookingResponse> confirmBooking(@PathVariable Long id) {
        return ApiResponse.<BookingResponse>builder()
                .result(bookingService.confirmBooking(id))
                .message("success")
                .build();
    }

    @PutMapping("/my-tours-bookings/{id}/reject")
    @PreAuthorize("hasAuthority('ROLE_BUSINESS')")
    ApiResponse<BookingResponse> rejectBooking(@PathVariable Long id) {
        return ApiResponse.<BookingResponse>builder()
                .result(bookingService.rejectBooking(id))
                .message("success")
                .build();
    }

    // ===== ADMIN =====

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    ApiResponse<PageResponse<BookingResponse>> getAllBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<BookingResponse>>builder()
                .result(bookingService.getAllBookings(page, size))
                .message("success")
                .build();
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    ApiResponse<PageResponse<BookingResponse>> searchAllBookings(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<BookingResponse>>builder()
                .result(bookingService.searchAllBookings(keyword, page, size))
                .message("success")
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    ApiResponse<BookingResponse> getBookingByIdForAdmin(@PathVariable Long id) {
        return ApiResponse.<BookingResponse>builder()
                .result(bookingService.getBookingByIdForAdmin(id))
                .message("success")
                .build();
    }

    @GetMapping("/departure/{departureId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    ApiResponse<PageResponse<BookingResponse>> getBookingsByDeparture(
            @PathVariable Long departureId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<BookingResponse>>builder()
                .result(bookingService.getBookingsByDeparture(departureId, page, size))
                .message("success")
                .build();
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    ApiResponse<PageResponse<BookingResponse>> getBookingsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<BookingResponse>>builder()
                .result(bookingService.getBookingsByUser(userId, page, size))
                .message("success")
                .build();
    }

    @GetMapping("/tour/{tourId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    ApiResponse<PageResponse<BookingResponse>> getBookingsByTour(
            @PathVariable Long tourId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<BookingResponse>>builder()
                .result(bookingService.getBookingsByTour(tourId, page, size))
                .message("success")
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    ApiResponse<BookingResponse> updateBookingForAdmin(@PathVariable Long id,
                                                       @RequestBody @Valid BookingUpdateRequest request) {
        return ApiResponse.<BookingResponse>builder()
                .result(bookingService.updateBookingForAdmin(id, request))
                .message("success")
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    ApiResponse<Void> deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
        return ApiResponse.<Void>builder()
                .message("success")
                .build();
    }
}