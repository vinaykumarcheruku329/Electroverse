package com.electroverse.repository;

import com.electroverse.entity.LifecycleAssessment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LifecycleAssessmentRepository extends JpaRepository<LifecycleAssessment, Long> {

    List<LifecycleAssessment> findByProductIdOrderByAssessmentDateDesc(Long productId);

    Optional<LifecycleAssessment> findTop1ByProductIdOrderByAssessmentDateDesc(Long productId);

    List<LifecycleAssessment> findTop20ByOrderByCreatedAtDesc();

    long countByProductId(Long productId);
}
