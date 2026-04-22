package com.Tu.Tu.repository;

import com.Tu.Tu.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByTourId(Long tourId, Pageable pageable);

    boolean existsByUserIdAndTourId(Long userId, Long tourId);

    // Dùng cho updateTourRating — cần List, không cần Page
    List<Review> findByTourId(Long tourId);

    // Business: lấy review theo tour mình sở hữu, tìm kiếm theo tên tour
    @Query(value = "SELECT r.* FROM reviews r " +
            "JOIN tours t ON r.Tourid = t.id " +
            "JOIN users u ON t.Userid = u.id " +
            "WHERE u.email = :email AND " +
            "(:keyword IS NULL OR t.name COLLATE Vietnamese_100_CI_AI LIKE CONCAT('%', :keyword, '%') COLLATE Vietnamese_100_CI_AI)",
            countQuery = "SELECT COUNT(1) FROM reviews r " +
                    "JOIN tours t ON r.Tourid = t.id " +
                    "JOIN users u ON t.Userid = u.id " +
                    "WHERE u.email = :email AND " +
                    "(:keyword IS NULL OR t.name COLLATE Vietnamese_100_CI_AI LIKE CONCAT('%', :keyword, '%') COLLATE Vietnamese_100_CI_AI)",
            nativeQuery = true)
    Page<Review> findByTourOwnerEmail(@Param("email") String email,
                                      @Param("keyword") String keyword,
                                      Pageable pageable);

    // Admin: tìm kiếm tất cả review theo tên tour
    @Query(value = "SELECT r.* FROM reviews r " +
            "JOIN tours t ON r.Tourid = t.id " +
            "WHERE (:keyword IS NULL OR t.name COLLATE Vietnamese_100_CI_AI LIKE CONCAT('%', :keyword, '%') COLLATE Vietnamese_100_CI_AI)",
            countQuery = "SELECT COUNT(1) FROM reviews r " +
                    "JOIN tours t ON r.Tourid = t.id " +
                    "WHERE (:keyword IS NULL OR t.name COLLATE Vietnamese_100_CI_AI LIKE CONCAT('%', :keyword, '%') COLLATE Vietnamese_100_CI_AI)",
            nativeQuery = true)
    Page<Review> searchAll(@Param("keyword") String keyword, Pageable pageable);

    Page<Review> findByTourIdAndRating(Long tourId, int rating, Pageable pageable);
}