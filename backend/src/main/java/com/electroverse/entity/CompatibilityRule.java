package com.electroverse.entity;

import com.electroverse.entity.enums.CompatibilityRelation;
import com.electroverse.entity.enums.CompatibilityResult;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "compatibility_rules", indexes = {
        @Index(name = "idx_compat_type", columnList = "rule_type")
})
public class CompatibilityRule extends BaseEntity {

    @Column(name = "rule_type", nullable = false, length = 80)
    private String ruleType;

    @Column(name = "source_type", nullable = false, length = 60)
    private String sourceType;

    @Column(name = "target_type", nullable = false, length = 60)
    private String targetType;

    @Column(name = "source_attribute", nullable = false, length = 80)
    private String sourceAttribute;

    @Column(name = "target_attribute", length = 80)
    private String targetAttribute;

    @Column(name = "expected_value", length = 250)
    private String expectedValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CompatibilityRelation relation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CompatibilityResult result;

    @Column(nullable = false, length = 700)
    private String explanation;

    @Column(nullable = false)
    private boolean active = true;

    public String getRuleType() {
        return ruleType;
    }

    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getSourceAttribute() {
        return sourceAttribute;
    }

    public void setSourceAttribute(String sourceAttribute) {
        this.sourceAttribute = sourceAttribute;
    }

    public String getTargetAttribute() {
        return targetAttribute;
    }

    public void setTargetAttribute(String targetAttribute) {
        this.targetAttribute = targetAttribute;
    }

    public String getExpectedValue() {
        return expectedValue;
    }

    public void setExpectedValue(String expectedValue) {
        this.expectedValue = expectedValue;
    }

    public CompatibilityRelation getRelation() {
        return relation;
    }

    public void setRelation(CompatibilityRelation relation) {
        this.relation = relation;
    }

    public CompatibilityResult getResult() {
        return result;
    }

    public void setResult(CompatibilityResult result) {
        this.result = result;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
