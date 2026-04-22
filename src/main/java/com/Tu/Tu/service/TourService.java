package com.Tu.Tu.service;

import com.Tu.Tu.constant.TourStatus;
import com.Tu.Tu.dto.request.TourCreateRequest;
import com.Tu.Tu.dto.request.TourUpdateRequest;
import com.Tu.Tu.dto.response.PageResponse;
import com.Tu.Tu.dto.response.TourResponse;
import com.Tu.Tu.entity.Image;
import com.Tu.Tu.entity.Tour;
import com.Tu.Tu.entity.User;
import com.Tu.Tu.exception.AppException;
import com.Tu.Tu.exception.ErrorCode;
import com.Tu.Tu.mapper.TourMapper;
import com.Tu.Tu.repository.ImageRepository;
import com.Tu.Tu.repository.BookingRepository;
import com.Tu.Tu.repository.DepartureRepository;
import com.Tu.Tu.repository.TourRepository;
import com.Tu.Tu.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TourService {

    TourRepository tourRepository;
    TourMapper tourMapper;
    ImageRepository imageRepository;
    UserRepository userRepository;

    // ===== PUBLIC =====

    public PageResponse<TourResponse> getAllActiveTours(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Tour> result = tourRepository.findAllActiveOrderByBookingCount(pageable);
        return toPageResponse(result);
    }

    public PageResponse<TourResponse> searchTours(String keyword, String departureLocation,
                                                  Integer durationDays, Long minPrice, Long maxPrice,
                                                  int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        String normalizedKeyword = keyword == null ? null : keyword.trim();
        if (normalizedKeyword != null && normalizedKeyword.isEmpty()) {
            normalizedKeyword = null;
        }

        String normalizedDepartureLocation = departureLocation == null ? null : departureLocation.trim();
        if (normalizedDepartureLocation != null && normalizedDepartureLocation.isEmpty()) {
            normalizedDepartureLocation = null;
        }

        Page<Tour> result = tourRepository.search(
                normalizedKeyword,
                normalizedDepartureLocation,
                durationDays,
                minPrice,
                maxPrice,
                pageable
        );
        return toPageResponse(result);
    }

    public PageResponse<TourResponse> searchToursByDepartureLocation(String departureKeyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        String normalizedKeyword = departureKeyword == null ? null : departureKeyword.trim();
        if (normalizedKeyword != null && normalizedKeyword.isEmpty()) {
            normalizedKeyword = null;
        }

        Page<Tour> result = tourRepository.searchByDepartureLocationLike(normalizedKeyword, pageable);
        return toPageResponse(result);
    }

    public TourResponse getTourById(Long id) {
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));
        return tourMapper.toResponse(tour);
    }

    // ===== BUSINESS ONLY =====

    @Transactional
    public TourResponse createTour(TourCreateRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (tourRepository.existsByCode(request.getCode())) {
            throw new AppException(ErrorCode.TOUR_CODE_EXISTED);
        }

        Tour tour = tourMapper.toEntity(request);
        tour.setUser(user);
        tour.setCreateAt(LocalDate.now());
        tour.setIsApproved(false);
        tour.setStatus(TourStatus.PENDING);
        tour.setAverageRating(0.0);
        tour.setReviewCount(0);

        log.info("highlights: {}", request.getHighlights());

        Tour savedTour = tourRepository.save(tour);

        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            List<Image> images = request.getImageUrls().stream()
                    .map(url -> Image.builder()
                            .image_url(url)
                            .createAt(LocalDate.now())
                            .tour(savedTour)
                            .build())
                    .toList();
            imageRepository.saveAll(images);
        }

        return tourMapper.toResponse(tourRepository.findById(savedTour.getId()).orElseThrow());
    }

    public PageResponse<TourResponse> getMyTours(int page, int size) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Pageable pageable = PageRequest.of(page, size);
        Page<Tour> result = tourRepository.findByUserId(user.getId(), pageable);
        return toPageResponse(result);
    }

    // ===== BUSINESS + ADMIN =====

    @Transactional
    public TourResponse updateTour(Long id, TourUpdateRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isAdmin = isAdmin();

        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        if (!isAdmin && !tour.getUser().getEmail().equals(email)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        tourMapper.updateTour(request, tour);

        if (request.getImageUrls() != null) {
            imageRepository.deleteByTourId(id);
            imageRepository.flush();

            List<Image> images = request.getImageUrls().stream()
                    .map(url -> Image.builder()
                            .image_url(url)
                            .createAt(LocalDate.now())
                            .tour(tour)
                            .build())
                    .toList();

            tour.getImageList().clear(); // clear list cũ trong memory
            tour.getImageList().addAll(imageRepository.saveAll(images)); // thêm list mới
        }

        return tourMapper.toResponse(tourRepository.save(tour));
    }

    /**
     * Xóa mềm (soft delete) — dùng cho business và admin trong vận hành thường ngày.
     * Set status = INACTIVE, isApproved = false.
     * Toàn bộ booking/payment/review/departure vẫn giữ nguyên để đối soát.
     * Idempotent: nếu tour đã INACTIVE thì trả về thành công luôn.
     */
    @Transactional
    public void softDeleteTour(Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isAdmin = isAdmin();

        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        if (!isAdmin && !tour.getUser().getEmail().equals(email)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Idempotent: đã inactive rồi thì không cần làm gì thêm
        if (TourStatus.INACTIVE.equals(tour.getStatus())) {
            return;
        }

        tour.setStatus(TourStatus.INACTIVE);
        tour.setIsApproved(false);
        tourRepository.save(tour);
    }

    /**
     * Hard delete — chỉ dành cho ADMIN, dùng để dọn dữ liệu rác.
     * Chỉ thực hiện khi tour thỏa mãn đủ 3 điều kiện cứng:
     *   1. Không có booking history liên quan.
     *   2. Không còn departure active (available/full) hoặc trong tương lai.
     *   3. Không vi phạm ràng buộc lịch sử nghiệp vụ.
     * Nếu fail bất kỳ điều kiện nào → throw lỗi nghiệp vụ rõ mã.
     */
    @Transactional
    public void hardDeleteTour(Long id) {
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        // Điều kiện 1: không có booking history
        if (tourRepository.existsBookingByTourId(id)) {
            throw new AppException(ErrorCode.TOUR_HAS_BOOKING_HISTORY);
        }

        // Điều kiện 2: không còn departure active hoặc tương lai
        if (tourRepository.existsActiveDepartureByTourId(id, LocalDate.now())) {
            throw new AppException(ErrorCode.TOUR_HAS_ACTIVE_DEPARTURE);
        }

        tourRepository.delete(tour);
    }

    // ===== ADMIN =====

    public PageResponse<TourResponse> getAllTours(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Tour> result = tourRepository.findAll(pageable);
        return toPageResponse(result);
    }

    public PageResponse<TourResponse> adminSearchTours(String keyword, String status, Boolean isApproved,
                                                       Long minPrice, Long maxPrice,
                                                       int page, int size,
                                                       String sortBy, String sortDir) {
        // Validate minPrice/maxPrice
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            throw new AppException(ErrorCode.INVALID_PRICE_RANGE);
        }

        String normalizedSortBy = normalizeSortColumn(sortBy);
        Sort sort = sortDir != null && sortDir.equalsIgnoreCase("desc")
            ? Sort.by(normalizedSortBy).descending()
            : Sort.by(normalizedSortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Tour> result = tourRepository.adminSearch(normalizeKeyword(keyword), status, isApproved, minPrice, maxPrice, pageable);
        return toPageResponse(result);
    }

    @Transactional
    public TourResponse approveTour(Long id) {
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));
        tour.setIsApproved(true);
        tour.setStatus(TourStatus.ACTIVE);
        return tourMapper.toResponse(tourRepository.save(tour));
    }

    @Transactional
    public TourResponse rejectTour(Long id) {
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));
        tour.setIsApproved(false);
        tour.setStatus(TourStatus.REJECTED);
        return tourMapper.toResponse(tourRepository.save(tour));
    }

    /**
     * Khôi phục tour từ INACTIVE (hoặc REJECTED) về PENDING để business chỉnh sửa,
     * sau đó submit lại cho admin duyệt.
     */
    @Transactional
    public TourResponse restoreTour(Long id) {
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));
        // Cho phép restore từ inactive hoặc rejected về pending
        tour.setStatus(TourStatus.PENDING);
        tour.setIsApproved(false);
        return tourMapper.toResponse(tourRepository.save(tour));
    }

    /**
     * Admin deactivate tour: chuyển sang INACTIVE.
     * Chặn nếu còn departure active/future để tránh ảnh hưởng booking hiện tại.
     */
    @Transactional
    public TourResponse deactivateTour(Long id) {
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        // Dùng exists query thay vì load toàn bộ object graph
        if (tourRepository.existsActiveDepartureByTourId(id, LocalDate.now())) {
            throw new AppException(ErrorCode.TOUR_HAS_ACTIVE_DEPARTURE);
        }

        tour.setStatus(TourStatus.INACTIVE);
        tour.setIsApproved(false);
        return tourMapper.toResponse(tourRepository.save(tour));
    }

    // ===== HELPER =====

    private boolean isAdmin() {
        return SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) return null;
        String normalized = keyword.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeSortColumn(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) return "create_at";

        return switch (sortBy) {
            case "createAt" -> "create_at";
            case "minPrice" -> "min_price";
            case "departureLocation" -> "departure_location";
            case "isApproved" -> "is_approved";
            default -> "create_at";
        };
    }

    private PageResponse<TourResponse> toPageResponse(Page<Tour> page) {
        return PageResponse.<TourResponse>builder()
                .content(page.getContent().stream().map(tourMapper::toResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Transactional
    public TourResponse assignVIPTour(Long id, Boolean isVIP) {
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));
        tour.setIsVIPTour(isVIP);
        return tourMapper.toResponse(tourRepository.save(tour));
    }
}