package com.electroverse.service;

import com.electroverse.dto.request.FeatureRequests;
import com.electroverse.dto.response.CatalogResponses;
import com.electroverse.dto.response.FeatureResponses;
import com.electroverse.entity.Product;
import com.electroverse.entity.ProductSpec;
import com.electroverse.entity.User;
import com.electroverse.exception.BadRequestException;
import com.electroverse.exception.ResourceNotFoundException;
import com.electroverse.mapper.DtoMapper;
import com.electroverse.entity.ComparisonHistory;
import com.electroverse.repository.ComparisonHistoryRepository;
import com.electroverse.repository.ProductRepository;
import com.electroverse.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class ComparisonService {

    private final ProductRepository productRepository;
    private final ComparisonHistoryRepository historyRepository;
    private final SecurityUtils securityUtils;

    public ComparisonService(ProductRepository productRepository,
                             ComparisonHistoryRepository historyRepository,
                             SecurityUtils securityUtils) {
        this.productRepository = productRepository;
        this.historyRepository = historyRepository;
        this.securityUtils = securityUtils;
    }

    @Transactional
    public FeatureResponses.Comparison compare(FeatureRequests.Comparison request) {
        List<Long> ids = request.productIds().stream().distinct().toList();
        if (ids.size() < 2 || ids.size() > 4) {
            throw new BadRequestException("Select between 2 and 4 distinct products to compare");
        }
        List<Product> products = new ArrayList<>();
        for (Long id : ids) {
            products.add(productRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id)));
        }

        List<CatalogResponses.ProductDetail> detail = products.stream().map(DtoMapper::toProductDetail).toList();
        List<FeatureResponses.SpecRow> rows = buildRows(products);
        List<String> priorities = request.priorities() != null ? request.priorities()
                : List.of("price", "rating", "performance", "battery", "storage");
        Map<String, Integer> best = new LinkedHashMap<>();
        for (String priority : priorities) {
            Integer idx = bestIndex(products, priority);
            if (idx != null) {
                best.put(priority, idx);
            }
        }

        try {
            User user = securityUtils.getCurrentUser();
            if (user != null) {
                ComparisonHistory history = new ComparisonHistory();
                history.setUser(user);
                history.setProductIds(ids.toString());
                history.setPriorities(priorities.toString());
                historyRepository.save(history);
            }
        } catch (Exception ignored) {
            // anonymous comparison - do not persist
        }

        return new FeatureResponses.Comparison(detail, rows, best);
    }

    private List<FeatureResponses.SpecRow> buildRows(List<Product> products) {
        Set<String> keys = new LinkedHashSet<>();
        Map<Long, Map<String, String>> valueMap = new LinkedHashMap<>();
        Map<String, String> groupMap = new LinkedHashMap<>();
        for (Product product : products) {
            Map<String, String> values = new LinkedHashMap<>();
            for (ProductSpec spec : product.getSpecs()) {
                keys.add(spec.getSpecKey());
                values.put(spec.getSpecKey(), spec.getSpecValue());
                groupMap.putIfAbsent(spec.getSpecKey(), spec.getSpecGroup());
            }
            valueMap.put(product.getId(), values);
        }

        List<FeatureResponses.SpecRow> rows = new ArrayList<>();
        for (String key : keys) {
            List<String> values = new ArrayList<>();
            for (Product product : products) {
                values.add(valueMap.get(product.getId()).getOrDefault(key, "—"));
            }
            boolean different = values.stream().distinct().count() > 1;
            rows.add(new FeatureResponses.SpecRow(key, groupMap.get(key), values, different, null));
        }
        return rows;
    }

    private Integer bestIndex(List<Product> products, String priority) {
        return switch (priority) {
            case "price", "value" -> argBest(products, (a, b) -> b.getPrice().compareTo(a.getPrice()));
            case "rating" -> argBest(products, (a, b) -> Double.compare(b.getRatingAverage(), a.getRatingAverage()));
            case "warranty" -> argBest(products, (a, b) -> Integer.compare(
                    b.getWarrantyMonths() == null ? 0 : b.getWarrantyMonths(),
                    a.getWarrantyMonths() == null ? 0 : a.getWarrantyMonths()));
            case "performance" -> argBestNumeric(products, "benchmark", "performance", "cpu score");
            case "battery" -> argBestNumeric(products, "battery");
            case "display" -> argBestNumeric(products, "display", "refresh");
            case "storage" -> argBestNumeric(products, "storage", "capacity");
            case "portability" -> argBestNumericInverted(products, "weight");
            default -> null;
        };
    }

    private Integer argBest(List<Product> products, java.util.Comparator<Product> comparator) {
        return products.stream().min(comparator)
                .map(products::indexOf)
                .orElse(null);
    }

    private Integer argBestNumeric(List<Product> products, String... keywords) {
        Integer best = null;
        double bestVal = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < products.size(); i++) {
            double v = maxNumeric(products.get(i), keywords);
            if (v > bestVal) {
                bestVal = v;
                best = i;
            }
        }
        return best;
    }

    private Integer argBestNumericInverted(List<Product> products, String... keywords) {
        Integer best = null;
        double bestVal = Double.POSITIVE_INFINITY;
        for (int i = 0; i < products.size(); i++) {
            double v = maxNumeric(products.get(i), keywords);
            if (v > 0 && v < bestVal) {
                bestVal = v;
                best = i;
            }
        }
        return best;
    }

    private double maxNumeric(Product product, String... keywords) {
        double max = 0;
        for (ProductSpec spec : product.getSpecs()) {
            String key = spec.getSpecKey().toLowerCase(Locale.ROOT);
            for (String kw : keywords) {
                if (key.contains(kw)) {
                    max = Math.max(max, extract(spec));
                }
            }
        }
        return max;
    }

    private double extract(ProductSpec spec) {
        if (spec.getNumericValue() != null) {
            return spec.getNumericValue();
        }
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+(?:\\.\\d+)?)")
                .matcher(spec.getSpecValue());
        if (m.find()) {
            try {
                return Double.parseDouble(m.group(1));
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }
        return 0;
    }
}
