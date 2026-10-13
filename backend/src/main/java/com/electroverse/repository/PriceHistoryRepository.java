package com.electroverse.repository;

import com.electroverse.entity.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {

    List<PriceHistory> findByProductIdOrderByRecordedAtAsc(Long productId);

    Optional<PriceHistory> findTop1ByProductIdOrderByRecordedAtDesc(Long productId);

    @Query("select min(ph.price) from PriceHistory ph where ph.product.id = :productId")
    java.math.BigDecimal findLowestPrice(Long productId);

    long countByProductId(Long productId);

    List<PriceHistory> findTop20ByOrderByRecordedAtDesc();

    long countByRecordedAtAfter(LocalDateTime since);
}
