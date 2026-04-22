package com.Tu.Tu.controller;

import com.Tu.Tu.dto.request.ReviewCreateRequest;
import com.Tu.Tu.dto.request.ReviewUpdateRequest;
import com.Tu.Tu.dto.response.ApiResponse;
import com.Tu.Tu.dto.response.PageResponse;
import com.Tu.Tu.dto.response.ReviewResponse;
import com.Tu.Tu.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;

    // ===== PUBLIC =====

    @GetMapping("/tour/{tourId}")
    ApiResponse<PageResponse<ReviewResponse>> getReviewsByTour(
            @PathVariable Long tourId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<ReviewResponse>>builder()
                .result(reviewService.getReviewsByTour(tourId, page, size))
                .message("success")
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<ReviewResponse> getReviewById(@PathVariable Long id) {
        return ApiResponse.<ReviewResponse>builder()
                .result(reviewService.getReviewById(id))
                .message("success")
                .build();
    }

    // ===== USER =====

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_USER')")
    ApiResponse<ReviewResponse> createReview(@RequestBody @Valid ReviewCreateRequest request) {
        return ApiResponse.<ReviewResponse>builder()
                .result(reviewService.createReview(request))
                .message("success")
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    ApiResponse<ReviewResponse> updateReview(@PathVariable Long id,
                                             @RequestBody @Valid ReviewUpdateRequest request) {
        return ApiResponse.<ReviewResponse>builder()
                .result(reviewService.updateReview(id, request))
                .message("success")
                .build();
    }

    @DeleteMapping("/my/{id}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    ApiResponse<Void> deleteMyReview(@PathVariable Long id) {
        reviewService.deleteMyReview(id);
        return ApiResponse.<Void>builder().message("success").build();
    }

    // ===== BUSINESS =====

    @GetMapping("/my-tours")
    @PreAuthorize("hasAuthority('ROLE_BUSINESS')")
    ApiResponse<PageResponse<ReviewResponse>> getReviewsByMyTours(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<ReviewResponse>>builder()
                .result(reviewService.getReviewsByMyTours(keyword, page, size))
                .message("success")
                .build();
    }

    // ===== ADMIN =====

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    ApiResponse<PageResponse<ReviewResponse>> getAllReviews(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<ReviewResponse>>builder()
                .result(reviewService.getAllReviews(keyword, page, size))
                .message("success")
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    ApiResponse<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ApiResponse.<Void>builder().message("success").build();
    }

    @GetMapping("/tour/{tourId}/rating")
    ApiResponse<PageResponse<ReviewResponse>> getReviewsByTourAndRating(
            @PathVariable Long tourId,
            @RequestParam int rating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<ReviewResponse>>builder()
                .result(reviewService.getReviewsByTourAndRating(tourId, rating, page, size))
                .message("success")
                .build();
    }
}