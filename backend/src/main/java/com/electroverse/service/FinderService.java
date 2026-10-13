package com.electroverse.service;

import com.electroverse.dto.request.FeatureRequests;
import com.electroverse.dto.response.CatalogResponses;
import com.electroverse.dto.response.FeatureResponses;
import com.electroverse.entity.FinderResult;
import com.electroverse.entity.FinderSession;
import com.electroverse.entity.LifecycleAssessment;
import com.electroverse.entity.Product;
import com.electroverse.entity.ProductSpec;
import com.electroverse.entity.User;
import com.electroverse.mapper.DtoMapper;
import com.electroverse.repository.FinderResultRepository;
import com.electroverse.repository.FinderSessionRepository;
import com.electroverse.repository.ProductRepository;
import com.electroverse.repository.LifecycleAssessmentRepository;
import com.electroverse.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class FinderService {

    public static final List<String> CRITERIA = List.of(
            "performance", "battery", "portability", "display", "storage",
            "energyEfficiency", "repairability", "value");

    private final ProductRepository productRepository;
    private final LifecycleAssessmentRepository lifecycleRepository;
    private final FinderSessionRepository sessionRepository;
    private final FinderResultRepository resultRepository;
    private final SecurityUtils securityUtils;

    public FinderService(ProductRepository productRepository,
                         LifecycleAssessmentRepository lifecycleRepository,
                         FinderSessionRepository sessionRepository,
                         FinderResultRepository resultRepository,
                         SecurityUtils securityUtils) {
        this.productRepository = productRepository;
        this.lifecycleRepository = lifecycleRepository;
        this.sessionRepository = sessionRepository;
        this.resultRepository = resultRepository;
        this.securityUtils = securityUtils;
    }

    @Transactional
    public FeatureResponses.Finder recommend(FeatureRequests.Finder request) {
        List<Product> candidates = productRepository.findAll(
                ProductSpecification.build(new ProductSpecification.ProductQuery(
                        null, request.categorySlug(), null, null, null, null, null, null, false, null)));

        if (candidates.isEmpty()) {
            FinderSession emptySession = persistSession(request);
            return new FeatureResponses.Finder(emptySession.getId(), request.categorySlug(),
                    request.budget(), request.purpose(), List.of());
        }

        Map<String, Integer> weights = resolveWeights(request.purpose(), request.priorities());
        CandidateStats stats = CandidateStats.of(candidates, request.budget());

        List<Scored> scored = new ArrayList<>();
        for (Product product : candidates) {
            Map<String, Double> criteria = criteriaScores(product, stats, request.budget());
            double total = 0;
            double weightSum = 0;
            for (Map.Entry<String, Integer> w : weights.entrySet()) {
                double c = criteria.getOrDefault(w.getKey(), 0.0);
                total += c * w.getValue();
                weightSum += w.getValue();
            }
            double score = weightSum > 0 ? (total / weightSum) * 100.0 : 0.0;
            score = BigDecimal.valueOf(score).setScale(1, RoundingMode.HALF_UP).doubleValue();
            boolean withinBudget = request.budget().compareTo(product.getPrice()) >= 0;
            if (!withinBudget) {
                score = score * 0.5;
            }
            scored.add(new Scored(product, score, criteria, weights, withinBudget));
        }

        scored.sort(Comparator.comparingDouble(Scored::score).reversed());

        int limit = request.maxResults() != null ? request.maxResults() : 6;
        List<Scored> top = scored.stream().limit(limit).toList();

        FinderSession session = persistSession(request);
        List<FeatureResponses.FinderResult> results = new ArrayList<>();
        int rank = 1;
        for (Scored s : top) {
            String explanation = buildExplanation(s);
            String specMatches = buildSpecMatches(s.product, weights.keySet());
            results.add(new FeatureResponses.FinderResult(
                    DtoMapper.toProductSummary(s.product), s.score, rank, explanation, specMatches, s.withinBudget));
            FinderResult entity = new FinderResult();
            entity.setSession(session);
            entity.setProduct(s.product);
            entity.setScore(s.score);
            entity.setRankPosition(rank);
            entity.setExplanation(explanation);
            entity.setSpecMatches(specMatches);
            entity.setWithinBudget(s.withinBudget);
            resultRepository.save(entity);
            rank++;
        }
        session.setResultCount(results.size());
        sessionRepository.save(session);

        return new FeatureResponses.Finder(session.getId(), request.categorySlug(),
                request.budget(), request.purpose(), results);
    }

    @Transactional(readOnly = true)
    public FeatureResponses.Finder getSession(Long id) {
        FinderSession session = sessionRepository.findById(id)
                .orElseThrow(() -> new com.electroverse.exception.ResourceNotFoundException("Finder session not found: " + id));
        List<FeatureResponses.FinderResult> results = resultRepository.findBySessionIdOrderByRankPositionAsc(id).stream()
                .map(r -> new FeatureResponses.FinderResult(
                        DtoMapper.toProductSummary(r.getProduct()),
                        r.getScore(), r.getRankPosition(), r.getExplanation(), r.getSpecMatches(), r.isWithinBudget()))
                .toList();
        return new FeatureResponses.Finder(session.getId(), session.getCategorySlug(),
                session.getBudget(), session.getPurpose(), results);
    }

    @Transactional(readOnly = true)
    public List<FeatureResponses.FinderSessionSummary> mySessions() {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            return List.of();
        }
        return sessionRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(s -> new FeatureResponses.FinderSessionSummary(
                        s.getId(), s.getCategorySlug(), s.getBudget(), s.getPurpose(),
                        s.getResultCount(), s.getCreatedAt()))
                .toList();
    }

    private FinderSession persistSession(FeatureRequests.Finder request) {
        FinderSession session = new FinderSession();
        User user = null;
        try {
            user = securityUtils.getCurrentUser();
        } catch (Exception ignored) {
            user = null;
        }
        session.setUser(user);
        session.setCategorySlug(request.categorySlug());
        session.setBudget(request.budget());
        session.setPurpose(request.purpose());
        session.setWeightsJson(toWeightsJson(resolveWeights(request.purpose(), request.priorities())));
        return sessionRepository.save(session);
    }

    public static Map<String, Integer> resolveWeights(String purpose, Map<String, Integer> priorities) {
        Map<String, Integer> weights = new LinkedHashMap<>();
        for (String c : CRITERIA) {
            weights.put(c, 1);
        }
        if (purpose != null) {
            switch (purpose.toLowerCase(Locale.ROOT)) {
                case "gaming" -> {
                    weights.put("performance", 5);
                    weights.put("display", 4);
                    weights.put("storage", 3);
                }
                case "programming" -> {
                    weights.put("performance", 5);
                    weights.put("storage", 4);
                    weights.put("display", 3);
                    weights.put("battery", 3);
                }
                case "study" -> {
                    weights.put("value", 5);
                    weights.put("battery", 4);
                    weights.put("portability", 4);
                }
                case "office" -> {
                    weights.put("battery", 5);
                    weights.put("portability", 4);
                    weights.put("performance", 3);
                }
                case "photography" -> {
                    weights.put("display", 5);
                    weights.put("performance", 4);
                    weights.put("storage", 4);
                }
                case "everyday" -> {
                    weights.put("value", 4);
                    weights.put("battery", 4);
                    weights.put("portability", 3);
                }
                default -> {
                }
            }
        }
        if (priorities != null) {
            priorities.forEach((key, value) -> {
                if (weights.containsKey(key) && value != null) {
                    weights.put(key, Math.max(1, Math.min(5, value)));
                }
            });
        }
        return weights;
    }

    private Map<String, Double> criteriaScores(Product product, CandidateStats stats, BigDecimal budget) {
        Map<String, Double> scores = new LinkedHashMap<>();

        double performance = specNumeric(product, "benchmark", "performance", "cpu score");
        if (performance <= 0) {
            performance = stats.priceNormalized(product);
        } else {
            performance = stats.normalize(performance, stats.maxPerformance);
        }
        scores.put("performance", clamp(performance));

        double battery = specNumeric(product, "battery life", "battery", "mah");
        battery = battery > 0 ? stats.normalize(battery, stats.maxBattery) : 0.6;
        scores.put("battery", clamp(battery));

        double weightKg = specNumeric(product, "weight");
        double portability = weightKg > 0 ? 1.0 - stats.normalize(weightKg, stats.maxWeight) : 0.6;
        scores.put("portability", clamp(portability));

        double display = specNumeric(product, "display", "refresh", "resolution score");
        display = display > 0 ? stats.normalize(display, stats.maxDisplay) : 0.6;
        scores.put("display", clamp(display));

        double storage = specNumeric(product, "storage", "capacity");
        storage = storage > 0 ? stats.normalize(storage, stats.maxStorage) : 0.5;
        scores.put("storage", clamp(storage));

        Optional<LifecycleAssessment> lifecycle = lifecycleRepository.findTop1ByProductIdOrderByAssessmentDateDesc(product.getId());
        double energy = lifecycle.map(l -> l.getEnergyEfficiency() / 100.0)
                .orElseGet(() -> {
                    double e = specNumeric(product, "energy", "efficiency", "tdp");
                    return e > 0 ? 1.0 - stats.normalize(e, stats.maxEnergy) : 0.6;
                });
        scores.put("energyEfficiency", clamp(energy));

        double repairability = lifecycle.map(l -> l.getRepairability() / 100.0)
                .orElseGet(() -> {
                    double r = specNumeric(product, "repair");
                    return r > 0 ? r / 100.0 : 0.5;
                });
        scores.put("repairability", clamp(repairability));

        double value;
        if (budget != null && budget.compareTo(BigDecimal.ZERO) > 0) {
            double budgetRatio = 1.0 - (product.getPrice().doubleValue() / budget.doubleValue());
            value = (clamp(budgetRatio) * 0.5) + ((1.0 - stats.priceNormalized(product)) * 0.5);
        } else {
            value = 1.0 - stats.priceNormalized(product);
        }
        scores.put("value", clamp(value));

        return scores;
    }

    private String buildExplanation(Scored scored) {
        List<Map.Entry<String, Double>> contributions = scored.criteria.entrySet().stream()
                .sorted(Comparator.comparingDouble(e -> -1 * (e.getValue() * scored.weights.getOrDefault(e.getKey(), 1))))
                .limit(3)
                .toList();
        StringBuilder sb = new StringBuilder();
        sb.append("Weighted score ").append(scored.score).append("/100. Top factors: ");
        List<String> parts = new ArrayList<>();
        for (Map.Entry<String, Double> e : contributions) {
            parts.add(pretty(e.getKey()) + " " + Math.round(e.getValue() * 100) + "%");
        }
        sb.append(String.join(", ", parts)).append(". ");
        sb.append(scored.withinBudget ? "Within your budget." : "Above your budget.");
        return sb.toString();
    }

    private String buildSpecMatches(Product product, java.util.Set<String> priorityKeys) {
        List<String> wanted = new ArrayList<>();
        for (String key : priorityKeys) {
            switch (key) {
                case "performance" -> wanted.add("Processor");
                case "display" -> wanted.add("Display");
                case "battery" -> wanted.add("Battery");
                case "storage" -> wanted.add("Storage");
                case "portability" -> wanted.add("Weight");
                default -> {
                }
            }
        }
        List<String> matches = new ArrayList<>();
        for (ProductSpec spec : product.getSpecs()) {
            for (String w : wanted) {
                if (spec.getSpecKey().toLowerCase(Locale.ROOT).contains(w.toLowerCase(Locale.ROOT))) {
                    matches.add(spec.getSpecKey() + ": " + spec.getSpecValue());
                }
            }
        }
        if (matches.isEmpty() && !product.getSpecs().isEmpty()) {
            product.getSpecs().stream().limit(3)
                    .forEach(s -> matches.add(s.getSpecKey() + ": " + s.getSpecValue()));
        }
        return String.join("; ", matches.stream().distinct().toList());
    }

    private double specNumeric(Product product, String... keywords) {
        double best = 0;
        for (ProductSpec spec : product.getSpecs()) {
            String key = spec.getSpecKey().toLowerCase(Locale.ROOT);
            for (String kw : keywords) {
                if (key.contains(kw)) {
                    double v = extractNumber(spec);
                    best = Math.max(best, v);
                }
            }
        }
        return best;
    }

    private double extractNumber(ProductSpec spec) {
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

    private double clamp(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }

    private String pretty(String key) {
        return switch (key) {
            case "energyEfficiency" -> "Energy efficiency";
            case "repairability" -> "Repairability";
            default -> key.substring(0, 1).toUpperCase(Locale.ROOT) + key.substring(1);
        };
    }

    private String toWeightsJson(Map<String, Integer> weights) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Integer> e : weights.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(e.getKey()).append("\":").append(e.getValue());
            first = false;
        }
        return sb.append("}").toString();
    }

    private record Scored(Product product, double score, Map<String, Double> criteria,
                          Map<String, Integer> weights, boolean withinBudget) {
    }

    private static final class CandidateStats {
        double maxPerformance;
        double maxBattery;
        double maxWeight;
        double maxDisplay;
        double maxStorage;
        double maxEnergy;
        double maxPrice;
        double minPrice;

        static CandidateStats of(List<Product> products, BigDecimal budget) {
            CandidateStats s = new CandidateStats();
            s.maxPrice = products.stream().mapToDouble(p -> p.getPrice().doubleValue()).max().orElse(1);
            s.minPrice = products.stream().mapToDouble(p -> p.getPrice().doubleValue()).min().orElse(0);
            s.maxPerformance = maxMetric(products, "benchmark", "performance", "cpu score");
            s.maxBattery = maxMetric(products, "battery life", "battery", "mah");
            s.maxWeight = maxMetric(products, "weight");
            s.maxDisplay = maxMetric(products, "display", "refresh", "resolution score");
            s.maxStorage = maxMetric(products, "storage", "capacity");
            s.maxEnergy = maxMetric(products, "energy", "efficiency", "tdp");
            return s;
        }

        private static double maxMetric(List<Product> products, String... keywords) {
            double max = 0;
            for (Product product : products) {
                for (ProductSpec spec : product.getSpecs()) {
                    String key = spec.getSpecKey().toLowerCase(Locale.ROOT);
                    for (String kw : keywords) {
                        if (key.contains(kw)) {
                            double v = extract(spec);
                            max = Math.max(max, v);
                        }
                    }
                }
            }
            return max > 0 ? max : 1;
        }

        private static double extract(ProductSpec spec) {
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

        double normalize(double value, double max) {
            if (max <= 0) return 0.5;
            return Math.max(0, Math.min(1, value / max));
        }

        double priceNormalized(Product product) {
            if (maxPrice <= 0 || maxPrice == minPrice) return 0.5;
            return (product.getPrice().doubleValue() - minPrice) / (maxPrice - minPrice);
        }
    }
}
