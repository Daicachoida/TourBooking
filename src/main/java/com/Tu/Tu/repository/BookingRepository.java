package com.Tu.Tu.repository;

import com.Tu.Tu.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Dùng cho phân trang
    Page<Booking> findByUserId(Long userId, Pageable pageable);

    Page<Booking> findByDepartureId(Long departureId, Pageable pageable);

    Page<Booking> findByDepartureTourId(Long tourId, Pageable pageable);

    Page<Booking> findByDepartureTourUserEmail(String email, Pageable pageable);

    // Dùng cho Dashboard
    List<Booking> findByDepartureTourUserEmail(String email);

    // User: search my bookings theo keyword (tourName/bookingCode), status, date range
    @Query(value = "SELECT b.* FROM bookings b " +
            "JOIN departures d ON b.Departureid = d.id " +
            "JOIN tours t ON d.Tourid = t.id " +
            "WHERE b.Userid = :userId AND " +
            "(:keyword IS NULL OR " +
            "t.name COLLATE Vietnamese_100_CI_AI LIKE CONCAT('%', :keyword, '%') COLLATE Vietnamese_100_CI_AI OR " +
            "b.booking_code COLLATE Vietnamese_100_CI_AI LIKE CONCAT('%', :keyword, '%') COLLATE Vietnamese_100_CI_AI) AND " +
            "(:status IS NULL OR b.status = :status) AND " +
            "(:fromDate IS NULL OR b.create_at >= :fromDate) AND " +
            "(:toDate IS NULL OR b.create_at <= :toDate)",
            countQuery = "SELECT COUNT(1) FROM bookings b " +
                    "JOIN departures d ON b.Departureid = d.id " +
                    "JOIN tours t ON d.Tourid = t.id " +
                    "WHERE b.Userid = :userId AND " +
                    "(:keyword IS NULL OR " +
                    "t.name COLLATE Vietnamese_100_CI_AI LIKE CONCAT('%', :keyword, '%') COLLATE Vietnamese_100_CI_AI OR " +
                    "b.booking_code COLLATE Vietnamese_100_CI_AI LIKE CONCAT('%', :keyword, '%') COLLATE Vietnamese_100_CI_AI) AND " +
                    "(:status IS NULL OR b.status = :status) AND " +
                    "(:fromDate IS NULL OR b.create_at >= :fromDate) AND " +
                    "(:toDate IS NULL OR b.create_at <= :toDate)",
            nativeQuery = true)
    Page<Booking> searchMyBookings(@Param("userId") Long userId,
                                   @Param("keyword") String keyword,
                                   @Param("status") String status,
                                   @Param("fromDate") LocalDate fromDate,
                                   @Param("toDate") LocalDate toDate,
                                   Pageable pageable);

    // Admin: tìm kiếm tất cả booking (theo tên tour hoặc booking code)
    @Query(value = "SELECT b.* FROM bookings b " +
            "JOIN departures d ON b.Departureid = d.id " +
            "JOIN tours t ON d.Tourid = t.id " +
            "WHERE (:keyword IS NULL OR " +
            "t.name COLLATE Vietnamese_100_CI_AI LIKE CONCAT('%', :keyword, '%') COLLATE Vietnamese_100_CI_AI OR " +
            "b.booking_code COLLATE Vietnamese_100_CI_AI LIKE CONCAT('%', :keyword, '%') COLLATE Vietnamese_100_CI_AI)",
            countQuery = "SELECT COUNT(1) FROM bookings b " +
                    "JOIN departures d ON b.Departureid = d.id " +
                    "JOIN tours t ON d.Tourid = t.id " +
                    "WHERE (:keyword IS NULL OR " +
                    "t.name COLLATE Vietnamese_100_CI_AI LIKE CONCAT('%', :keyword, '%') COLLATE Vietnamese_100_CI_AI OR " +
                    "b.booking_code COLLATE Vietnamese_100_CI_AI LIKE CONCAT('%', :keyword, '%') COLLATE Vietnamese_100_CI_AI)",
            nativeQuery = true)
    Page<Booking> searchAll(@Param("keyword") String keyword, Pageable pageable);

    // Business: tìm kiếm booking theo tour mình sở hữu
    @Query(value = "SELECT b.* FROM bookings b " +
            "JOIN departures d ON b.Departureid = d.id " +
            "JOIN tours t ON d.Tourid = t.id " +
            "JOIN users u ON t.Userid = u.id " +
            "WHERE u.email = :email AND " +
            "(:keyword IS NULL OR " +
            "t.name COLLATE Vietnamese_100_CI_AI LIKE CONCAT('%', :keyword, '%') COLLATE Vietnamese_100_CI_AI OR " +
            "b.booking_code COLLATE Vietnamese_100_CI_AI LIKE CONCAT('%', :keyword, '%') COLLATE Vietnamese_100_CI_AI)",
            countQuery = "SELECT COUNT(1) FROM bookings b " +
                    "JOIN departures d ON b.Departureid = d.id " +
                    "JOIN tours t ON d.Tourid = t.id " +
                    "JOIN users u ON t.Userid = u.id " +
                    "WHERE u.email = :email AND " +
                    "(:keyword IS NULL OR " +
                    "t.name COLLATE Vietnamese_100_CI_AI LIKE CONCAT('%', :keyword, '%') COLLATE Vietnamese_100_CI_AI OR " +
                    "b.booking_code COLLATE Vietnamese_100_CI_AI LIKE CONCAT('%', :keyword, '%') COLLATE Vietnamese_100_CI_AI)",
            nativeQuery = true)
    Page<Booking> searchByTourOwner(@Param("email") String email,
                                    @Param("keyword") String keyword,
                                    Pageable pageable);

    @Query(value = "SELECT b.* FROM bookings b WHERE b.status = 'pending' AND b.create_at <= :cutoffDate",
            nativeQuery = true)
    List<Booking> findPendingBookingsOlderThan(@Param("cutoffDate") LocalDate cutoffDate);
}