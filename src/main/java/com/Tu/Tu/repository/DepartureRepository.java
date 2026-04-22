package com.Tu.Tu.repository;

import com.Tu.Tu.entity.Departure;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DepartureRepository extends JpaRepository<Departure, Long> {
    Page<Departure> findByTourIdAndStatusAndDepartureDateGreaterThanEqual(
            Long tourId,
            String status,
            LocalDate departureDate,
            Pageable pageable
    );

    Page<Departure> findByTourId(Long tourId, Pageable pageable);

    // Admin search: keyword (tên tour), status, date range
    @Query(value = "SELECT d.* FROM departures d " +
            "JOIN tours t ON d.Tourid = t.id " +
            "WHERE (:keyword IS NULL OR t.name COLLATE Vietnamese_100_CI_AI LIKE CONCAT('%', :keyword, '%') COLLATE Vietnamese_100_CI_AI) AND " +
            "(:status IS NULL OR d.status = :status) AND " +
            "(:dateFrom IS NULL OR d.departure_date >= :dateFrom) AND " +
            "(:dateTo IS NULL OR d.departure_date <= :dateTo)",
            countQuery = "SELECT COUNT(1) FROM departures d " +
                    "JOIN tours t ON d.Tourid = t.id " +
                    "WHERE (:keyword IS NULL OR t.name COLLATE Vietnamese_100_CI_AI LIKE CONCAT('%', :keyword, '%') COLLATE Vietnamese_100_CI_AI) AND " +
                    "(:status IS NULL OR d.status = :status) AND " +
                    "(:dateFrom IS NULL OR d.departure_date >= :dateFrom) AND " +
                    "(:dateTo IS NULL OR d.departure_date <= :dateTo)",
            nativeQuery = true)
    Page<Departure> adminSearch(@Param("keyword") String keyword,
                                @Param("status") String status,
                                @Param("dateFrom") LocalDate dateFrom,
                                @Param("dateTo") LocalDate dateTo,
                                Pageable pageable);

    // Business: lọc departures của tour mình sở hữu theo tourStatus và departureStatus
    @Query(value = "SELECT d.* FROM departures d " +
            "JOIN tours t ON d.Tourid = t.id " +
            "JOIN users u ON t.Userid = u.id " +
            "WHERE u.email = :email AND " +
            "(:tourStatus IS NULL OR t.status = :tourStatus) AND " +
            "(:departureStatus IS NULL OR d.status = :departureStatus) AND " +
            "(:dateFrom IS NULL OR d.departure_date >= :dateFrom) AND " +
            "(:dateTo IS NULL OR d.departure_date <= :dateTo)",
            countQuery = "SELECT COUNT(1) FROM departures d " +
                    "JOIN tours t ON d.Tourid = t.id " +
                    "JOIN users u ON t.Userid = u.id " +
                    "WHERE u.email = :email AND " +
                    "(:tourStatus IS NULL OR t.status = :tourStatus) AND " +
                    "(:departureStatus IS NULL OR d.status = :departureStatus) AND " +
                    "(:dateFrom IS NULL OR d.departure_date >= :dateFrom) AND " +
                    "(:dateTo IS NULL OR d.departure_date <= :dateTo)",
            nativeQuery = true)
    Page<Departure> findByBusinessEmail(@Param("email") String email,
                                        @Param("tourStatus") String tourStatus,
                                        @Param("departureStatus") String departureStatus,
                                        @Param("dateFrom") LocalDate dateFrom,
                                        @Param("dateTo") LocalDate dateTo,
                                        Pageable pageable);

    // Scheduler: tìm các departure cần chuyển sang DEPARTED
    // (departureDate <= hôm nay VÀ status còn available hoặc full)
//    @Query(value = "SELECT d.* FROM departures d WHERE " +
//            "d.departure_date <= :today AND " +
//            "d.status IN ('available', 'full')",
//            nativeQuery = true)
//    List<Departure> findDeparturesToMarkDeparted(@Param("today") LocalDate today);

    // Bulk update: chuyển available/full sang departed theo ngày (dùng trong scheduler)
    @Modifying
    @Query(value = "UPDATE departures SET status = 'departed' WHERE " +
            "departure_date <= :today AND " +
            "status IN ('available', 'full')",
            nativeQuery = true)
    int bulkMarkDeparted(@Param("today") LocalDate today);
}