package com.electroverse.repository;

import com.electroverse.entity.PriceAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PriceAlertRepository extends JpaRepository<PriceAlert, Long> {

    List<PriceAlert> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<PriceAlert> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    List<PriceAlert> findByProductIdAndActiveTrueAndTriggeredFalse(Long productId);

    List<PriceAlert> findByActiveTrue();

    long countByTriggeredTrue();
}
