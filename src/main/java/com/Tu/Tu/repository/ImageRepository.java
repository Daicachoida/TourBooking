package com.Tu.Tu.repository;

import com.Tu.Tu.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    List<Image> findByTourId(Long tourId);

    void deleteByTourId(Long tourId);
}