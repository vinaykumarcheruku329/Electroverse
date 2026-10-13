package com.electroverse.repository;

import com.electroverse.entity.Review;
import com.electroverse.entity.enums.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByProductIdAndStatusOrderByCreatedAtDesc(Long productId, ReviewStatus status, Pageable pageable);

    List<Review> findByProductIdAndStatusOrderByCreatedAtDesc(Long productId, ReviewStatus status);

    boolean existsByProductIdAndUserId(Long productId, Long userId);

    long countByProductIdAndStatus(Long productId, ReviewStatus status);

    List<Review> findAllByOrderByCreatedAtDesc();

    long countByStatus(ReviewStatus status);
}
