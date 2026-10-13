package com.electroverse.repository;

import com.electroverse.entity.CompatibilityRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompatibilityRuleRepository extends JpaRepository<CompatibilityRule, Long> {

    List<CompatibilityRule> findByActiveTrue();

    List<CompatibilityRule> findByActiveTrueAndRuleType(String ruleType);

    List<CompatibilityRule> findBySourceTypeAndTargetTypeAndActiveTrue(String sourceType, String targetType);

    List<CompatibilityRule> findAllByOrderByRuleTypeAscIdAsc();
}
