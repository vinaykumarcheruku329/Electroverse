package com.electroverse.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class CatalogResponses {

    private CatalogResponses() {
    }

    public record Category(
            Long id,
            String name,
            String slug,
            String description,
            String icon,
            long productCount
    ) {
    }

    public record Image(
            String url,
            String altText,
            Integer sortOrder
    ) {
    }

    public record Spec(
            String group,
            String key,
            String value,
            Double numericValue
    ) {
    }

    public record ProductSummary(
            Long id,
            String name,
            String slug,
            String brand,
            String categoryName,
            String categorySlug,
            BigDecimal price,
            BigDecimal compareAtPrice,
            String currency,
            int stock,
            double ratingAverage,
            int ratingCount,
            String primaryImageUrl,
            boolean featured,
            Integer warrantyMonths
    ) {
    }

    public record ProductDetail(
            Long id,
            String name,
            String slug,
            String brand,
            Long categoryId,
            String categoryName,
            String categorySlug,
            String description,
            BigDecimal price,
            BigDecimal compareAtPrice,
            String currency,
            int stock,
            double ratingAverage,
            int ratingCount,
            Integer warrantyMonths,
            String primaryImageUrl,
            LocalDate releaseDate,
            boolean featured,
            boolean active,
            LocalDateTime createdAt,
            List<Image> images,
            List<Spec> specs
    ) {
    }

    public record Paged<T>(
            List<T> content,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean last
    ) {
    }
}
