package com.electroverse.dto.request;

import com.electroverse.entity.enums.CompatibilityRelation;
import com.electroverse.entity.enums.CompatibilityResult;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

public final class FeatureRequests {

    private FeatureRequests() {
    }

    public record Finder(
            @NotBlank(message = "Category is required")
            String categorySlug,

            @NotNull(message = "Budget is required")
            @DecimalMin(value = "0.0", inclusive = false, message = "Budget must be positive")
            BigDecimal budget,

            @Size(max = 60)
            String purpose,

            Map<String, Integer> priorities,

            @Min(1)
            @Max(12)
            Integer maxResults
    ) {
    }

    public record Comparison(
            @NotNull(message = "Product ids are required")
            @Size(min = 2, max = 4, message = "Select between 2 and 4 products to compare")
            java.util.List<Long> productIds,

            java.util.List<String> priorities
    ) {
    }

    public record CompatibilityCheck(
            @NotBlank(message = "Source component type is required")
            String sourceType,

            @NotNull(message = "Source attributes are required")
            Map<String, String> sourceAttributes,

            @NotBlank(message = "Target component type is required")
            String targetType,

            @NotNull(message = "Target attributes are required")
            Map<String, String> targetAttributes
    ) {
    }

    public record PriceSnapshot(
            @NotNull(message = "Price is required")
            @DecimalMin(value = "0.0", inclusive = false, message = "Price must be positive")
            BigDecimal price,

            LocalDateTime recordedAt,

            @Size(max = 120)
            String source,

            Boolean demoData
    ) {
    }

    public record Lifecycle(
            @NotNull(message = "Product id is required")
            Long productId,

            @NotNull @Min(0) @Max(100)
            Integer repairability,

            @NotNull @Min(0) @Max(100)
            Integer upgradeability,

            @NotNull @Min(0) @Max(100)
            Integer manufacturerSupport,

            @NotNull @Min(0) @Max(100)
            Integer warranty,

            @NotNull @Min(0) @Max(100)
            Integer energyEfficiency,

            @Size(max = 1500)
            String explanation,

            LocalDate assessmentDate,

            @Size(max = 30)
            String methodologyVersion,

            Boolean demoData
    ) {
    }

    public record CompatibilityRule(
            @NotBlank(message = "Rule type is required")
            @Size(max = 80)
            String ruleType,

            @NotBlank(message = "Source type is required")
            @Size(max = 60)
            String sourceType,

            @NotBlank(message = "Target type is required")
            @Size(max = 60)
            String targetType,

            @NotBlank(message = "Source attribute is required")
            @Size(max = 80)
            String sourceAttribute,

            @Size(max = 80)
            String targetAttribute,

            @Size(max = 250)
            String expectedValue,

            @NotNull(message = "Relation is required")
            CompatibilityRelation relation,

            @NotNull(message = "Result is required")
            CompatibilityResult result,

            @NotBlank(message = "Explanation is required")
            @Size(max = 700)
            String explanation,

            Boolean active
    ) {
    }
}
