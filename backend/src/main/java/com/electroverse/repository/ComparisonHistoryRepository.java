package com.electroverse.repository;

import com.electroverse.entity.ComparisonHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComparisonHistoryRepository extends JpaRepository<ComparisonHistory, Long> {

    List<ComparisonHistory> findTop20ByUserIdOrderByCreatedAtDesc(Long userId);
}
