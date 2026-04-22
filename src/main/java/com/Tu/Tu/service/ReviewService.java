package com.Tu.Tu.service;

import com.Tu.Tu.constant.BookingStatus;
import com.Tu.Tu.dto.request.ReviewCreateRequest;
import com.Tu.Tu.dto.request.ReviewUpdateRequest;
import com.Tu.Tu.dto.response.PageResponse;
import com.Tu.Tu.dto.response.ReviewResponse;
import com.Tu.Tu.entity.Review;
import com.Tu.Tu.entity.Tour;
import com.Tu.Tu.entity.User;
import com.Tu.Tu.exception.AppException;
import com.Tu.Tu.exception.ErrorCode;
import com.Tu.Tu.mapper.ReviewMapper;
import com.Tu.Tu.repository.BookingRepository;
import com.Tu.Tu.repository.ReviewRepository;
import com.Tu.Tu.repository.TourRepository;
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
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReviewService {

    ReviewRepository reviewRepository;
    TourRepository tourRepository;
    UserRepository userRepository;
    BookingRepository bookingRepository;
    ReviewMapper reviewMapper;

    // ===== PUBLIC =====

    public PageResponse<ReviewResponse> getReviewsByTour(Long tourId, int page, int size) {
        tourRepository.findById(tourId)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));
        Pageable pageable = PageRequest.of(page, size);
        Page<Review> result = reviewRepository.findByTourId(tourId, pageable);
        return toPageResponse(result);
    }

    public ReviewResponse getReviewById(Long id) {
        return reviewMapper.toResponse(
                reviewRepository.findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND))
        );
    }

    public PageResponse<ReviewResponse> getReviewsByTourAndRating(Long tourId, int rating, int page, int size) {
        tourRepository.findById(tourId)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        if (rating < 1 || rating > 5) {
            throw new AppException(ErrorCode.INVALID_RATING);
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Review> result = reviewRepository.findByTourIdAndRating(tourId, rating, pageable);
        return toPageResponse(result);
    }

    // ===== USER =====

    @Transactional
    public ReviewResponse createReview(ReviewCreateRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Tour tour = tourRepository.findById(request.getTourId())
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        boolean hasConfirmedBooking = bookingRepository.findByUserId(user.getId(), PageRequest.of(0, Integer.MAX_VALUE))
                .getContent().stream()
                .anyMatch(b -> b.getDeparture().getTour().getId().equals(tour.getId())
                        && BookingStatus.CONFIRMED.equals(b.getStatus()));
        if (!hasConfirmedBooking) {
            throw new AppException(ErrorCode.REVIEW_NOT_ELIGIBLE);
        }

        if (reviewRepository.existsByUserIdAndTourId(user.getId(), tour.getId())) {
            throw new AppException(ErrorCode.REVIEW_ALREADY_EXISTED);
        }

        Review review = reviewMapper.toEntity(request);
        review.setCreateAt(LocalDate.now());
        review.setUser(user);
        review.setTour(tour);

        Review savedReview = reviewRepository.save(review);
        updateTourRating(tour);

        return reviewMapper.toResponse(savedReview);
    }

    @Transactional
    public ReviewResponse updateReview(Long id, ReviewUpdateRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));

        if (!review.getUser().getEmail().equals(email)) {
            throw new AppException(ErrorCode.REVIEW_UNAUTHORIZED);
        }

        reviewMapper.updateReview(request, review);
        Review savedReview = reviewRepository.save(review);
        updateTourRating(review.getTour());

        return reviewMapper.toResponse(savedReview);
    }

    @Transactional
    public void deleteMyReview(Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));

        if (!review.getUser().getEmail().equals(email)) {
            throw new AppException(ErrorCode.REVIEW_UNAUTHORIZED);
        }

        Tour tour = review.getTour();
        reviewRepository.delete(review);
        updateTourRating(tour);
    }

    // ===== BUSINESS =====

    public PageResponse<ReviewResponse> getReviewsByMyTours(String keyword, int page, int size) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Pageable pageable = PageRequest.of(page, size);
        Page<Review> result = reviewRepository.findByTourOwnerEmail(email, normalizeKeyword(keyword), pageable);
        return toPageResponse(result);
    }

    // ===== ADMIN =====

    public PageResponse<ReviewResponse> getAllReviews(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Review> result = reviewRepository.searchAll(normalizeKeyword(keyword), pageable);
        return toPageResponse(result);
    }

    @Transactional
    public void deleteReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));

        Tour tour = review.getTour();
        reviewRepository.delete(review);
        updateTourRating(tour);
    }

    // ===== HELPER =====

    private void updateTourRating(Tour tour) {
        List<Review> reviews = reviewRepository.findByTourId(tour.getId());
        int count = reviews.size();
        double average = count == 0 ? 0
                : reviews.stream().mapToInt(Review::getRating).average().orElse(0);

        tour.setReviewCount(count);
        tour.setAverageRating(Math.round(average * 10.0) / 10.0);
        tourRepository.save(tour);
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) return null;
        String normalized = keyword.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private PageResponse<ReviewResponse> toPageResponse(Page<Review> page) {
        return PageResponse.<ReviewResponse>builder()
                .content(page.getContent().stream().map(reviewMapper::toResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}