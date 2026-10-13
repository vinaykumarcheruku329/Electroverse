package com.electroverse.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class CatalogRequests {

    private CatalogRequests() {
    }

    public record Category(
            @NotBlank(message = "Name is required")
            @Size(max = 120)
            String name,

            @Size(max = 140)
            String slug,

            @Size(max = 500)
            String description,

            @Size(max = 120)
            String icon
    ) {
    }

    public record Spec(
            @Size(max = 80)
            String specGroup,

            @NotBlank(message = "Specification key is required")
            @Size(max = 120)
            String specKey,

            @NotBlank(message = "Specification value is required")
            @Size(max = 500)
            String specValue,

            Double numericValue,

            Integer sortOrder
    ) {
    }

    public record Product(
            @NotBlank(message = "Name is required")
            @Size(max = 200)
            String name,

            @Size(max = 220)
            String slug,

            @Size(max = 120)
            String brand,

            @NotNull(message = "Category is required")
            Long categoryId,

            @Size(max = 4000)
            String description,

            @NotNull(message = "Price is required")
            @DecimalMin(value = "0.0", inclusive = false, message = "Price must be positive")
            BigDecimal price,

            @DecimalMin(value = "0.0", message = "Compare price cannot be negative")
            BigDecimal compareAtPrice,

            @Size(max = 3)
            String currency,

            @PositiveOrZero(message = "Stock cannot be negative")
            Integer stock,

            @PositiveOrZero(message = "Warranty cannot be negative")
            Integer warrantyMonths,

            @Size(max = 700)
            String primaryImageUrl,

            LocalDate releaseDate,

            Boolean featured,

            Boolean active,

            @Valid
            List<Spec> specs,

            List<String> images
    ) {
    }

    public record StockUpdate(
            @NotNull(message = "Stock is required")
            @Min(value = 0, message = "Stock cannot be negative")
            Integer stock
    ) {
    }
}
