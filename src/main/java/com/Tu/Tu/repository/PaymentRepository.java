package com.Tu.Tu.repository;

import com.Tu.Tu.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Page<Payment> findByBookingId(Long bookingId, Pageable pageable);

    Page<Payment> findByBookingUserEmail(String email, Pageable pageable);

    Page<Payment> findByBookingUserId(Long userId, Pageable pageable);

    List<Payment> findByBookingDepartureTourUserEmail(String email);

    Optional<Payment> findByTransactionRef(String transactionRef);

    @Query(value = "SELECT p.* FROM payments p WHERE p.status = 'pending' AND p.expired_at < :now",
            nativeQuery = true)
    List<Payment> findExpiredPendingPayments(@Param("now") LocalDateTime now);

    @Query(value = "SELECT p.* FROM payments p " +
            "JOIN bookings b ON p.Bookingid = b.id " +
            "JOIN departures d ON b.Departureid = d.id " +
            "JOIN tours t ON d.Tourid = t.id " +
            "JOIN users u ON t.Userid = u.id " +
            "WHERE u.email = :email AND " +
            "(:keyword IS NULL OR t.name COLLATE Vietnamese_100_CI_AI LIKE CONCAT('%', :keyword, '%') COLLATE Vietnamese_100_CI_AI)",
            countQuery = "SELECT COUNT(1) FROM payments p " +
                    "JOIN bookings b ON p.Bookingid = b.id " +
                    "JOIN departures d ON b.Departureid = d.id " +
                    "JOIN tours t ON d.Tourid = t.id " +
                    "JOIN users u ON t.Userid = u.id " +
                    "WHERE u.email = :email AND " +
                    "(:keyword IS NULL OR t.name COLLATE Vietnamese_100_CI_AI LIKE CONCAT('%', :keyword, '%') COLLATE Vietnamese_100_CI_AI)",
            nativeQuery = true)
    Page<Payment> findByTourOwnerEmail(@Param("email") String email,
                                       @Param("keyword") String keyword,
                                       Pageable pageable);

    @Query(value = "SELECT p.* FROM payments p " +
            "JOIN bookings b ON p.Bookingid = b.id " +
            "JOIN departures d ON b.Departureid = d.id " +
            "JOIN tours t ON d.Tourid = t.id " +
            "WHERE (:keyword IS NULL OR t.name COLLATE Vietnamese_100_CI_AI LIKE CONCAT('%', :keyword, '%') COLLATE Vietnamese_100_CI_AI)",
            countQuery = "SELECT COUNT(1) FROM payments p " +
                    "JOIN bookings b ON p.Bookingid = b.id " +
                    "JOIN departures d ON b.Departureid = d.id " +
                    "JOIN tours t ON d.Tourid = t.id " +
                    "WHERE (:keyword IS NULL OR t.name COLLATE Vietnamese_100_CI_AI LIKE CONCAT('%', :keyword, '%') COLLATE Vietnamese_100_CI_AI)",
            nativeQuery = true)
    Page<Payment> searchAll(@Param("keyword") String keyword, Pageable pageable);


    /**
     * Doanh thu theo từng năm, từ năm fromYear trở đi.
     */
    @Query(value = "SELECT YEAR(payment_time) AS yr, SUM(amount) AS total " +
            "FROM payments " +
            "WHERE status = 'success' AND YEAR(payment_time) >= :fromYear " +
            "GROUP BY YEAR(payment_time) " +
            "ORDER BY YEAR(payment_time)",
            nativeQuery = true)
    List<Object[]> revenueByYear(@Param("fromYear") int fromYear);

    /**
     * Doanh thu theo từng tháng trong một năm cụ thể.
     */
    @Query(value = "SELECT MONTH(payment_time) AS mth, SUM(amount) AS total " +
            "FROM payments " +
            "WHERE status = 'success' AND YEAR(payment_time) = :year " +
            "GROUP BY MONTH(payment_time) " +
            "ORDER BY MONTH(payment_time)",
            nativeQuery = true)
    List<Object[]> revenueByMonthOfYear(@Param("year") int year);

    /**
     * Doanh thu theo từng ngày trong một tháng cụ thể.
     */
    @Query(value = "SELECT DAY(payment_time) AS dy, SUM(amount) AS total " +
            "FROM payments " +
            "WHERE status = 'success' AND YEAR(payment_time) = :year AND MONTH(payment_time) = :month " +
            "GROUP BY DAY(payment_time) " +
            "ORDER BY DAY(payment_time)",
            nativeQuery = true)
    List<Object[]> revenueByDayOfMonth(@Param("year") int year, @Param("month") int month);

    /**
     * Doanh thu VIP vs Thường theo năm.
     */
    @Query(value = "SELECT t.isviptour, SUM(p.amount) AS total " +
            "FROM payments p " +
            "JOIN bookings b ON p.Bookingid = b.id " +
            "JOIN departures d ON b.Departureid = d.id " +
            "JOIN tours t ON d.Tourid = t.id " +
            "WHERE p.status = 'success' AND YEAR(p.payment_time) = :year " +
            "GROUP BY t.isviptour",
            nativeQuery = true)
    List<Object[]> revenueVipVsNormalByYear(@Param("year") int year);

    /**
     * Doanh thu VIP vs Thường — lọc theo email owner (dùng cho Business dashboard).
     */
    @Query(value = "SELECT t.isviptour, SUM(p.amount) AS total " +
            "FROM payments p " +
            "JOIN bookings b ON p.Bookingid = b.id " +
            "JOIN departures d ON b.Departureid = d.id " +
            "JOIN tours t ON d.Tourid = t.id " +
            "JOIN users u ON t.Userid = u.id " +
            "WHERE p.status = 'success' AND YEAR(p.payment_time) = :year AND u.email = :email " +
            "GROUP BY t.isviptour",
            nativeQuery = true)
    List<Object[]> revenueVipVsNormalByYearAndOwner(@Param("year") int year, @Param("email") String email);

    /**
     * Doanh thu theo năm — lọc theo email owner (dùng cho Business dashboard).
     */
    @Query(value = "SELECT YEAR(p.payment_time) AS yr, SUM(p.amount) AS total " +
            "FROM payments p " +
            "JOIN bookings b ON p.Bookingid = b.id " +
            "JOIN departures d ON b.Departureid = d.id " +
            "JOIN tours t ON d.Tourid = t.id " +
            "JOIN users u ON t.Userid = u.id " +
            "WHERE p.status = 'success' AND YEAR(p.payment_time) >= :fromYear AND u.email = :email " +
            "GROUP BY YEAR(p.payment_time) " +
            "ORDER BY YEAR(p.payment_time)",
            nativeQuery = true)
    List<Object[]> revenueByYearAndOwner(@Param("fromYear") int fromYear, @Param("email") String email);

    /**
     * Doanh thu theo tháng trong năm — lọc theo email owner (dùng cho Business dashboard).
     */
    @Query(value = "SELECT MONTH(p.payment_time) AS mth, SUM(p.amount) AS total " +
            "FROM payments p " +
            "JOIN bookings b ON p.Bookingid = b.id " +
            "JOIN departures d ON b.Departureid = d.id " +
            "JOIN tours t ON d.Tourid = t.id " +
            "JOIN users u ON t.Userid = u.id " +
            "WHERE p.status = 'success' AND YEAR(p.payment_time) = :year AND u.email = :email " +
            "GROUP BY MONTH(p.payment_time) " +
            "ORDER BY MONTH(p.payment_time)",
            nativeQuery = true)
    List<Object[]> revenueByMonthOfYearAndOwner(@Param("year") int year, @Param("email") String email);

    /**
     * Doanh thu theo ngày trong tháng — lọc theo email owner (dùng cho Business dashboard).
     */
    @Query(value = "SELECT DAY(p.payment_time) AS dy, SUM(p.amount) AS total " +
            "FROM payments p " +
            "JOIN bookings b ON p.Bookingid = b.id " +
            "JOIN departures d ON b.Departureid = d.id " +
            "JOIN tours t ON d.Tourid = t.id " +
            "JOIN users u ON t.Userid = u.id " +
            "WHERE p.status = 'success' AND YEAR(p.payment_time) = :year AND MONTH(p.payment_time) = :month AND u.email = :email " +
            "GROUP BY DAY(p.payment_time) " +
            "ORDER BY DAY(p.payment_time)",
            nativeQuery = true)
    List<Object[]> revenueByDayOfMonthAndOwner(@Param("year") int year, @Param("month") int month, @Param("email") String email);
}