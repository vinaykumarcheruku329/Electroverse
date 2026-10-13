package com.electroverse.service;

import com.electroverse.entity.Product;
import com.electroverse.entity.ProductSpec;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ProductSpecification {

    private ProductSpecification() {
    }

    public static Specification<Product> build(ProductQuery query) {
        return (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (!query.includeInactive()) {
                predicates.add(cb.isTrue(root.get("active")));
            }
            if (query.categorySlug() != null && !query.categorySlug().isBlank()) {
                predicates.add(cb.equal(root.join("category", JoinType.INNER).get("slug"), query.categorySlug()));
            }
            if (query.brand() != null && !query.brand().isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("brand")), query.brand().toLowerCase()));
            }
            if (query.minPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), query.minPrice()));
            }
            if (query.maxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), query.maxPrice()));
            }
            if (query.minRating() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("ratingAverage"), query.minRating()));
            }
            if (Boolean.TRUE.equals(query.inStock())) {
                predicates.add(cb.greaterThan(root.get("stock"), 0));
            }
            if (Boolean.TRUE.equals(query.featured())) {
                predicates.add(cb.isTrue(root.get("featured")));
            }
            if (query.search() != null && !query.search().isBlank()) {
                String pattern = "%" + query.search().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("brand")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)
                ));
            }
            if (query.specs() != null && !query.specs().isEmpty()) {
                for (Map.Entry<String, String> entry : query.specs().entrySet()) {
                    String key = entry.getKey().toLowerCase();
                    String value = entry.getValue().toLowerCase();
                    Subquery<Long> subquery = criteriaQuery.subquery(Long.class);
                    Root<ProductSpec> specRoot = subquery.from(ProductSpec.class);
                    subquery.select(specRoot.get("product").get("id"));
                    subquery.where(
                            cb.equal(specRoot.get("product"), root),
                            cb.equal(cb.lower(specRoot.get("specKey")), key),
                            cb.like(cb.lower(specRoot.get("specValue")), "%" + value + "%")
                    );
                    predicates.add(cb.exists(subquery));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public record ProductQuery(
            String search,
            String categorySlug,
            String brand,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Double minRating,
            Boolean inStock,
            Boolean featured,
            boolean includeInactive,
            Map<String, String> specs
    ) {
    }
}
