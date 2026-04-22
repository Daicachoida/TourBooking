package com.Tu.Tu.repository;

import com.Tu.Tu.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String Email);

    Optional<User> findByEmail(String Email);

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<User> findWithRolesByEmail(String email);

    // Tìm kiếm user theo tên hoặc email (tương đối)
                @Query(value = "SELECT u.* FROM users u WHERE " +
                    "(:keyword IS NULL OR " +
                    "u.full_name COLLATE Latin1_General_100_CI_AI LIKE CONCAT('%', :keyword, '%') COLLATE Latin1_General_100_CI_AI OR " +
                    "u.email COLLATE Latin1_General_100_CI_AI LIKE CONCAT('%', :keyword, '%') COLLATE Latin1_General_100_CI_AI)",
                    countQuery = "SELECT COUNT(1) FROM users u WHERE " +
                        "(:keyword IS NULL OR " +
                        "u.full_name COLLATE Latin1_General_100_CI_AI LIKE CONCAT('%', :keyword, '%') COLLATE Latin1_General_100_CI_AI OR " +
                        "u.email COLLATE Latin1_General_100_CI_AI LIKE CONCAT('%', :keyword, '%') COLLATE Latin1_General_100_CI_AI)",
                    nativeQuery = true)
    Page<User> search(@Param("keyword") String keyword, Pageable pageable);
}