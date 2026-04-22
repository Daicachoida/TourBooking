package com.Tu.Tu.repository;

import com.Tu.Tu.entity.Tour;
import com.Tu.Tu.entity.Booking;
import com.Tu.Tu.entity.Departure;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TourRepository extends JpaRepository<Tour, Long> {

    Page<Tour> findByIsApprovedAndStatus(Boolean isApproved, String status, Pageable pageable);

    // Public: lấy tour active, sort theo tổng booking nhiều nhất
    @Query(value = "SELECT t.* FROM tours t " +
            "WHERE t.is_approved = 1 AND t.status = 'active' " +
            "ORDER BY (" +
            "SELECT COUNT(1) FROM bookings b " +
            "JOIN departures d ON b.Departureid = d.id " +
            "WHERE d.Tourid = t.id" +
            ") DESC",
            countQuery = "SELECT COUNT(1) FROM tours t WHERE t.is_approved = 1 AND t.status = 'active'",
            nativeQuery = true)
    Page<Tour> findAllActiveOrderByBookingCount(Pageable pageable);

    // Dùng cho phân trang
    Page<Tour> findByUserId(Long userId, Pageable pageable);

    // Dùng cho Dashboard
    List<Tour> findByUserId(Long userId);

    boolean existsByCode(String code);

    /**
     * Kiểm tra tour có booking history không (tránh load toàn bộ object graph).
     * Dùng khi validate trước hard delete.
     */
    @Query(value = "SELECT CASE WHEN COUNT(1) > 0 THEN CAST(1 AS BIT) ELSE CAST(0 AS BIT) END " +
            "FROM bookings b " +
            "JOIN departures d ON b.Departureid = d.id " +
            "WHERE d.Tourid = :tourId",
            nativeQuery = true)
    boolean existsBookingByTourId(@Param("tourId") Long tourId);

    /**
     * Kiểm tra tour còn departure đang active (available/full) hoặc trong tương lai không.
     * Bao gồm hôm nay vì departure ngày hôm nay vẫn có thể đang diễn ra.
     */
    @Query(value = "SELECT CASE WHEN COUNT(1) > 0 THEN CAST(1 AS BIT) ELSE CAST(0 AS BIT) END " +
            "FROM departures d " +
            "WHERE d.Tourid = :tourId " +
            "AND d.status IN ('available', 'full') " +
            "AND d.departure_date >= :today",
            nativeQuery = true)
    boolean existsActiveDepartureByTourId(@Param("tourId") Long tourId,
                                          @Param("today") LocalDate today);

    // Search public (user xem)
    @Query(value = "SELECT t.* FROM tours t WHERE " +
            "(:keyword IS NULL OR t.name COLLATE Vietnamese_100_CI_AI LIKE CONCAT('%', :keyword, '%') COLLATE Vietnamese_100_CI_AI) AND " +
            "(:departureLocation IS NULL OR t.departure_location COLLATE Vietnamese_100_CI_AI LIKE CONCAT('%', :departureLocation, '%') COLLATE Vietnamese_100_CI_AI) AND " +
            "(:durationDays IS NULL OR t.duration_days = :durationDays) AND " +
            "(:minPrice IS NULL OR t.min_price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR t.min_price <= :maxPrice) AND " +
            "t.is_approved = 1 AND t.status = 'active'",
            countQuery = "SELECT COUNT(1) FROM tours t WHERE " +
                    "(:keyword IS NULL OR t.name COLLATE Vietnamese_100_CI_AI LIKE CONCAT('%', :keyword, '%') COLLATE Vietnamese_100_CI_AI) AND " +
                    "(:departureLocation IS NULL OR t.departure_location COLLATE Vietnamese_100_CI_AI LIKE CONCAT('%', :departureLocation, '%') COLLATE Vietnamese_100_CI_AI) AND " +
                    "(:durationDays IS NULL OR t.duration_days = :durationDays) AND " +
                    "(:minPrice IS NULL OR t.min_price >= :minPrice) AND " +
                    "(:maxPrice IS NULL OR t.min_price <= :maxPrice) AND " +
                    "t.is_approved = 1 AND t.status = 'active'",
            nativeQuery = true)
    Page<Tour> search(@Param("keyword") String keyword,
                      @Param("departureLocation") String departureLocation,
                      @Param("durationDays") Integer durationDays,
                      @Param("minPrice") Long minPrice,
                      @Param("maxPrice") Long maxPrice,
                      Pageable pageable);

    // Public: tìm tour theo điểm khởi hành (tìm kiếm tương đối, không phân biệt hoa thường)
    @Query(value = "SELECT t.* FROM tours t WHERE " +
            "(:departureKeyword IS NULL OR t.departure_location COLLATE Vietnamese_100_CI_AI LIKE CONCAT('%', :departureKeyword, '%') COLLATE Vietnamese_100_CI_AI) AND " +
            "t.is_approved = 1 AND t.status = 'active'",
            countQuery = "SELECT COUNT(1) FROM tours t WHERE " +
                    "(:departureKeyword IS NULL OR t.departure_location COLLATE Vietnamese_100_CI_AI LIKE CONCAT('%', :departureKeyword, '%') COLLATE Vietnamese_100_CI_AI) AND " +
                    "t.is_approved = 1 AND t.status = 'active'",
            nativeQuery = true)
    Page<Tour> searchByDepartureLocationLike(@Param("departureKeyword") String departureKeyword,
                                             Pageable pageable);

    // Search admin: filter theo keyword, status, isApproved, minPrice, maxPrice
    @Query(value = "SELECT t.* FROM tours t WHERE " +
            "(:keyword IS NULL OR " +
            "t.name COLLATE Vietnamese_100_CI_AI LIKE CONCAT('%', :keyword, '%') COLLATE Vietnamese_100_CI_AI OR " +
            "t.departure_location COLLATE Vietnamese_100_CI_AI LIKE CONCAT('%', :keyword, '%') COLLATE Vietnamese_100_CI_AI OR " +
            "t.description COLLATE Vietnamese_100_CI_AI LIKE CONCAT('%', :keyword, '%') COLLATE Vietnamese_100_CI_AI) AND " +
            "(:status IS NULL OR t.status = :status) AND " +
            "(:isApproved IS NULL OR t.is_approved = :isApproved) AND " +
            "(:minPrice IS NULL OR t.min_price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR t.min_price <= :maxPrice)",
            countQuery = "SELECT COUNT(1) FROM tours t WHERE " +
                    "(:keyword IS NULL OR " +
                    "t.name COLLATE Vietnamese_100_CI_AI LIKE CONCAT('%', :keyword, '%') COLLATE Vietnamese_100_CI_AI OR " +
                    "t.departure_location COLLATE Vietnamese_100_CI_AI LIKE CONCAT('%', :keyword, '%') COLLATE Vietnamese_100_CI_AI OR " +
                    "t.description COLLATE Vietnamese_100_CI_AI LIKE CONCAT('%', :keyword, '%') COLLATE Vietnamese_100_CI_AI) AND " +
                    "(:status IS NULL OR t.status = :status) AND " +
                    "(:isApproved IS NULL OR t.is_approved = :isApproved) AND " +
                    "(:minPrice IS NULL OR t.min_price >= :minPrice) AND " +
                    "(:maxPrice IS NULL OR t.min_price <= :maxPrice)",
            nativeQuery = true)
    Page<Tour> adminSearch(@Param("keyword") String keyword,
                           @Param("status") String status,
                           @Param("isApproved") Boolean isApproved,
                           @Param("minPrice") Long minPrice,
                           @Param("maxPrice") Long maxPrice,
                           Pageable pageable);
}