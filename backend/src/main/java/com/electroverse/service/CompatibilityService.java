package com.electroverse.service;

import com.electroverse.dto.request.FeatureRequests;
import com.electroverse.dto.response.FeatureResponses;
import com.electroverse.entity.CompatibilityRule;
import com.electroverse.entity.enums.CompatibilityResult;
import com.electroverse.exception.ResourceNotFoundException;
import com.electroverse.mapper.DtoMapper;
import com.electroverse.repository.CompatibilityRuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class CompatibilityService {

    private final CompatibilityRuleRepository ruleRepository;

    public CompatibilityService(CompatibilityRuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    @Transactional(readOnly = true)
    public FeatureResponses.Compatibility check(FeatureRequests.CompatibilityCheck request) {
        String sourceType = request.sourceType().trim();
        String targetType = request.targetType().trim();

        List<CompatibilityRule> all = ruleRepository.findByActiveTrue();
        List<Applicable> applicable = new ArrayList<>();
        for (CompatibilityRule rule : all) {
            boolean direct = rule.getSourceType().equalsIgnoreCase(sourceType)
                    && rule.getTargetType().equalsIgnoreCase(targetType);
            boolean reversed = rule.getSourceType().equalsIgnoreCase(targetType)
                    && rule.getTargetType().equalsIgnoreCase(sourceType);
            if (!direct && !reversed) {
                continue;
            }
            Map<String, String> sourceAttrs = reversed ? request.targetAttributes() : request.sourceAttributes();
            Map<String, String> targetAttrs = reversed ? request.sourceAttributes() : request.targetAttributes();
            applicable.add(evaluate(rule, sourceAttrs, targetAttrs));
        }

        if (applicable.isEmpty()) {
            return new FeatureResponses.Compatibility(
                    CompatibilityResult.UNKNOWN.name(),
                    "No compatibility rule is registered for " + sourceType + " + " + targetType
                            + ". Compatibility cannot be verified from stored rules.",
                    null, null, false, sourceType, targetType);
        }

        for (Applicable a : applicable) {
            if (a.rule.getResult() == CompatibilityResult.INCOMPATIBLE && a.conditionHolds) {
                return toResponse(a, CompatibilityResult.INCOMPATIBLE);
            }
        }
        for (Applicable a : applicable) {
            if (a.rule.getResult() == CompatibilityResult.COMPATIBLE && a.conditionHolds) {
                return toResponse(a, CompatibilityResult.COMPATIBLE);
            }
        }
        for (Applicable a : applicable) {
            if (a.rule.getResult() == CompatibilityResult.CONDITIONAL && a.conditionHolds) {
                return toResponse(a, CompatibilityResult.CONDITIONAL);
            }
        }

        boolean hasCompatibleRule = applicable.stream()
                .anyMatch(a -> a.rule.getResult() == CompatibilityResult.COMPATIBLE);
        boolean hasConditionalRule = applicable.stream()
                .anyMatch(a -> a.rule.getResult() == CompatibilityResult.CONDITIONAL);

        if (hasCompatibleRule) {
            return new FeatureResponses.Compatibility(
                    CompatibilityResult.INCOMPATIBLE.name(),
                    "The components do not satisfy any registered compatibility requirement for "
                            + sourceType + " + " + targetType + ".",
                    applicable.get(0).rule.getId(),
                    applicable.get(0).rule.getRuleType(),
                    true, sourceType, targetType);
        }
        if (hasConditionalRule) {
            return new FeatureResponses.Compatibility(
                    CompatibilityResult.UNKNOWN.name(),
                    "Compatibility is conditional but the required conditions could not be verified from the provided attributes.",
                    applicable.get(0).rule.getId(),
                    applicable.get(0).rule.getRuleType(),
                    false, sourceType, targetType);
        }
        return new FeatureResponses.Compatibility(
                CompatibilityResult.UNKNOWN.name(),
                "No matching compatibility rule condition could be verified.",
                null, null, false, sourceType, targetType);
    }

    private FeatureResponses.Compatibility toResponse(Applicable a, CompatibilityResult result) {
        return new FeatureResponses.Compatibility(
                result.name(),
                a.explanation,
                a.rule.getId(),
                a.rule.getRuleType(),
                true,
                a.rule.getSourceType(),
                a.rule.getTargetType());
    }

    private Applicable evaluate(CompatibilityRule rule, Map<String, String> sourceAttrs, Map<String, String> targetAttrs) {
        String actual = sourceAttrs == null ? null : sourceAttrs.get(rule.getSourceAttribute());
        String expected = rule.getExpectedValue();
        if (expected == null && rule.getTargetAttribute() != null && targetAttrs != null) {
            expected = targetAttrs.get(rule.getTargetAttribute());
        }
        boolean holds = compare(actual, expected, rule);
        String explanation = rule.getExplanation();
        if (holds && actual != null && expected != null) {
            explanation = rule.getExplanation() + " ("
                    + rule.getSourceAttribute() + " = '" + actual + "' matched '" + expected + "')";
        }
        return new Applicable(rule, holds, explanation);
    }

    private boolean compare(String actual, String expected, CompatibilityRule rule) {
        if (actual == null || actual.isBlank() || expected == null || expected.isBlank()) {
            return false;
        }
        String a = actual.trim().toLowerCase(Locale.ROOT);
        String e = expected.trim().toLowerCase(Locale.ROOT);
        return switch (rule.getRelation()) {
            case EQUALS -> a.equals(e);
            case NOT_EQUALS -> !a.equals(e);
            case CONTAINS -> a.contains(e);
            case IN -> List.of(e.split(",")).stream().map(String::trim).anyMatch(v -> v.equals(a));
            case GREATER_OR_EQUAL -> parseDouble(a) >= parseDouble(e);
            case LESS_OR_EQUAL -> parseDouble(a) <= parseDouble(e);
        };
    }

    private double parseDouble(String v) {
        try {
            return Double.parseDouble(v.replaceAll("[^0-9.]", ""));
        } catch (RuntimeException ex) {
            return Double.NaN;
        }
    }

    @Transactional(readOnly = true)
    public List<FeatureResponses.CompatibilityRule> listRules() {
        return ruleRepository.findAllByOrderByRuleTypeAscIdAsc().stream()
                .map(DtoMapper::toCompatibilityRule).toList();
    }

    @Transactional(readOnly = true)
    public List<String> listTypes() {
        return ruleRepository.findByActiveTrue().stream()
                .flatMap(r -> java.util.stream.Stream.of(r.getSourceType(), r.getTargetType()))
                .distinct()
                .sorted()
                .toList();
    }

    @Transactional
    public FeatureResponses.CompatibilityRule createRule(FeatureRequests.CompatibilityRule request) {
        CompatibilityRule rule = new CompatibilityRule();
        apply(rule, request);
        return DtoMapper.toCompatibilityRule(ruleRepository.save(rule));
    }

    @Transactional
    public FeatureResponses.CompatibilityRule updateRule(Long id, FeatureRequests.CompatibilityRule request) {
        CompatibilityRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compatibility rule not found: " + id));
        apply(rule, request);
        return DtoMapper.toCompatibilityRule(ruleRepository.save(rule));
    }

    @Transactional
    public void deleteRule(Long id) {
        CompatibilityRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compatibility rule not found: " + id));
        ruleRepository.delete(rule);
    }

    private void apply(CompatibilityRule rule, FeatureRequests.CompatibilityRule request) {
        rule.setRuleType(request.ruleType().trim());
        rule.setSourceType(request.sourceType().trim());
        rule.setTargetType(request.targetType().trim());
        rule.setSourceAttribute(request.sourceAttribute().trim());
        rule.setTargetAttribute(request.targetAttribute());
        rule.setExpectedValue(request.expectedValue());
        rule.setRelation(request.relation());
        rule.setResult(request.result());
        rule.setExplanation(request.explanation());
        rule.setActive(request.active() == null || request.active());
    }

    private record Applicable(CompatibilityRule rule, boolean conditionHolds, String explanation) {
    }
}
