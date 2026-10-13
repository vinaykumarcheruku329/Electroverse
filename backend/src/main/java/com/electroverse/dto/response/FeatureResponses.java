package com.electroverse.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public final class FeatureResponses {

    private FeatureResponses() {
    }

    public record FinderResult(
            CatalogResponses.ProductSummary product,
            double score,
            int rank,
            String explanation,
            String specMatches,
            boolean withinBudget
    ) {
    }

    public record Finder(
            Long sessionId,
            String categorySlug,
            BigDecimal budget,
            String purpose,
            List<FinderResult> results
    ) {
    }

    public record FinderSessionSummary(
            Long id,
            String categorySlug,
            BigDecimal budget,
            String purpose,
            int resultCount,
            LocalDateTime createdAt
    ) {
    }

    public record SpecRow(
            String label,
            String group,
            List<String> values,
            boolean different,
            Integer bestIndex
    ) {
    }

    public record Comparison(
            List<CatalogResponses.ProductDetail> products,
            List<SpecRow> rows,
            Map<String, Integer> bestIndexByPriority
    ) {
    }

    public record Compatibility(
            String result,
            String reason,
            Long ruleId,
            String ruleType,
            boolean verified,
            String sourceType,
            String targetType
    ) {
    }

    public record PricePoint(
            LocalDateTime date,
            BigDecimal price,
            String source,
            boolean demoData
    ) {
    }

    public record PriceHistory(
            Long productId,
            List<PricePoint> points,
            BigDecimal lowestPrice,
            BigDecimal latestPrice,
            LocalDateTime latestDate,
            String source,
            boolean demoData
    ) {
    }

    public record Lifecycle(
            Long id,
            Long productId,
            String productName,
            int repairability,
            int upgradeability,
            int manufacturerSupport,
            int warranty,
            int energyEfficiency,
            double overall,
            String explanation,
            LocalDate assessmentDate,
            String methodologyVersion,
            boolean demoData
    ) {
    }

    public record CompatibilityRule(
            Long id,
            String ruleType,
            String sourceType,
            String targetType,
            String sourceAttribute,
            String targetAttribute,
            String expectedValue,
            String relation,
            String result,
            String explanation,
            boolean active
    ) {
    }
}
