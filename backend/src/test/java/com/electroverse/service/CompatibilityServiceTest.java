package com.electroverse.service;

import com.electroverse.dto.request.FeatureRequests;
import com.electroverse.dto.response.FeatureResponses;
import com.electroverse.entity.CompatibilityRule;
import com.electroverse.entity.enums.CompatibilityRelation;
import com.electroverse.entity.enums.CompatibilityResult;
import com.electroverse.repository.CompatibilityRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompatibilityServiceTest {

    @Mock
    private CompatibilityRuleRepository ruleRepository;

    private CompatibilityService service;

    @BeforeEach
    void setUp() {
        service = new CompatibilityService(ruleRepository);
    }

    private CompatibilityRule rule(String sourceType, String targetType, String sourceAttr, String targetAttr,
                                   String expected, CompatibilityRelation relation, CompatibilityResult result) {
        CompatibilityRule rule = new CompatibilityRule();
        rule.setRuleType(sourceType + "_" + targetType);
        rule.setSourceType(sourceType);
        rule.setTargetType(targetType);
        rule.setSourceAttribute(sourceAttr);
        rule.setTargetAttribute(targetAttr);
        rule.setExpectedValue(expected);
        rule.setRelation(relation);
        rule.setResult(result);
        rule.setExplanation("seed rule");
        rule.setActive(true);
        rule.setId(1L);
        return rule;
    }

    @Test
    void returnsCompatibleWhenSocketMatches() {
        when(ruleRepository.findByActiveTrue()).thenReturn(List.of(
                rule("CPU", "MOTHERBOARD", "Socket", "Socket", null,
                        CompatibilityRelation.EQUALS, CompatibilityResult.COMPATIBLE)));

        FeatureRequests.CompatibilityCheck request = new FeatureRequests.CompatibilityCheck(
                "CPU", Map.of("Socket", "AM5"),
                "MOTHERBOARD", Map.of("Socket", "AM5"));

        FeatureResponses.Compatibility result = service.check(request);

        assertThat(result.result()).isEqualTo("COMPATIBLE");
        assertThat(result.verified()).isTrue();
        assertThat(result.reason()).contains("seed rule");
    }

    @Test
    void returnsIncompatibleWhenNoCompatibleRequirementHolds() {
        when(ruleRepository.findByActiveTrue()).thenReturn(List.of(
                rule("CPU", "MOTHERBOARD", "Socket", "Socket", null,
                        CompatibilityRelation.EQUALS, CompatibilityResult.COMPATIBLE)));

        FeatureRequests.CompatibilityCheck request = new FeatureRequests.CompatibilityCheck(
                "CPU", Map.of("Socket", "AM5"),
                "MOTHERBOARD", Map.of("Socket", "LGA1700"));

        FeatureResponses.Compatibility result = service.check(request);

        assertThat(result.result()).isEqualTo("INCOMPATIBLE");
        assertThat(result.verified()).isTrue();
    }

    @Test
    void returnsUnknownWhenNoRulesExist() {
        when(ruleRepository.findByActiveTrue()).thenReturn(List.of());

        FeatureRequests.CompatibilityCheck request = new FeatureRequests.CompatibilityCheck(
                "CAMERA", Map.of("Mount", "E"),
                "LENS", Map.of("Mount", "Z"));

        FeatureResponses.Compatibility result = service.check(request);

        assertThat(result.result()).isEqualTo("UNKNOWN");
        assertThat(result.verified()).isFalse();
    }

    @Test
    void supportsReversedSourceTargetOrder() {
        when(ruleRepository.findByActiveTrue()).thenReturn(List.of(
                rule("GPU", "PSU", "Recommended PSU (W)", "Wattage (W)", null,
                        CompatibilityRelation.LESS_OR_EQUAL, CompatibilityResult.COMPATIBLE)));

        FeatureRequests.CompatibilityCheck request = new FeatureRequests.CompatibilityCheck(
                "PSU", Map.of("Wattage (W)", "850"),
                "GPU", Map.of("Recommended PSU (W)", "750"));

        assertThat(service.check(request).result()).isEqualTo("COMPATIBLE");
    }
}
