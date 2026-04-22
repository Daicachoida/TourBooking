package com.Tu.Tu.repository;

import com.Tu.Tu.entity.DeparturePrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeparturePriceRepository extends JpaRepository<DeparturePrice, Long> {
    List<DeparturePrice> findByDepartureId(Long departureId);
}
