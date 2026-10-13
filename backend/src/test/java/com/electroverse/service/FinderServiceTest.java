package com.electroverse.service;

import com.electroverse.dto.request.FeatureRequests;
import com.electroverse.dto.response.FeatureResponses;
import com.electroverse.entity.Category;
import com.electroverse.entity.FinderResult;
import com.electroverse.entity.FinderSession;
import com.electroverse.entity.Product;
import com.electroverse.entity.ProductSpec;
import com.electroverse.exception.ResourceNotFoundException;
import com.electroverse.repository.FinderResultRepository;
import com.electroverse.repository.FinderSessionRepository;
import com.electroverse.repository.LifecycleAssessmentRepository;
import com.electroverse.repository.ProductRepository;
import com.electroverse.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinderServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private LifecycleAssessmentRepository lifecycleRepository;
    @Mock private FinderSessionRepository sessionRepository;
    @Mock private FinderResultRepository resultRepository;
    @Mock private SecurityUtils securityUtils;

    private FinderService service;

    @BeforeEach
    void setUp() {
        service = new FinderService(productRepository, lifecycleRepository, sessionRepository, resultRepository, securityUtils);
        lenient().when(securityUtils.getCurrentUser()).thenThrow(new ResourceNotFoundException("anonymous"));
        lenient().when(lifecycleRepository.findTop1ByProductIdOrderByAssessmentDateDesc(anyLong()))
                .thenReturn(Optional.empty());
        lenient().when(sessionRepository.save(any(FinderSession.class))).thenAnswer(inv -> {
            FinderSession s = inv.getArgument(0);
            if (s.getId() == null) {
                s.setId(99L);
            }
            return s;
        });
        lenient().when(resultRepository.save(any(FinderResult.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Product laptop(String name, String price, double benchmark) {
        Category category = new Category();
        category.setName("Laptops");
        category.setSlug("laptops");
        Product product = new Product();
        product.setName(name);
        product.setSlug(CategoryService.toSlug(name));
        product.setCategory(category);
        product.setPrice(new BigDecimal(price));
        product.setStock(10);
        product.setActive(true);
        product.setCurrency("INR");
        product.setRatingAverage(4.0);
        ProductSpec spec = new ProductSpec();
        spec.setSpecKey("Benchmark Score");
        spec.setSpecValue(String.valueOf((int) benchmark));
        spec.setNumericValue(benchmark);
        spec.setProduct(product);
        product.getSpecs().add(spec);
        return product;
    }

    @Test
    void ranksHigherPerformanceProductFirstWhenPerformancePrioritised() {
        Product strong = laptop("Strong", "1500.00", 1500);
        Product weak = laptop("Weak", "900.00", 800);
        when(productRepository.findAll(any(Specification.class))).thenReturn(List.of(strong, weak));

        FeatureRequests.Finder request = new FeatureRequests.Finder(
                "laptops", new BigDecimal("2000.00"), "programming",
                java.util.Map.of("performance", 5), 5);

        FeatureResponses.Finder result = service.recommend(request);

        assertThat(result.results()).hasSize(2);
        assertThat(result.results().get(0).product().name()).isEqualTo("Strong");
        assertThat(result.results().get(0).score()).isGreaterThanOrEqualTo(result.results().get(1).score());
        assertThat(result.results().get(0).withinBudget()).isTrue();
    }

    @Test
    void marksProductAboveBudgetWhenBudgetTooLow() {
        Product expensive = laptop("Expensive", "5000.00", 1500);
        when(productRepository.findAll(any(Specification.class))).thenReturn(List.of(expensive));

        FeatureRequests.Finder request = new FeatureRequests.Finder(
                "laptops", new BigDecimal("1000.00"), "everyday", null, 5);

        FeatureResponses.Finder result = service.recommend(request);

        assertThat(result.results()).hasSize(1);
        assertThat(result.results().get(0).withinBudget()).isFalse();
    }

    @Test
    void returnsEmptyResultsWhenNoProducts() {
        when(productRepository.findAll(any(Specification.class))).thenReturn(List.of());

        FeatureRequests.Finder request = new FeatureRequests.Finder(
                "gpus", new BigDecimal("1000.00"), "gaming", null, 5);

        FeatureResponses.Finder result = service.recommend(request);

        assertThat(result.results()).isEmpty();
        assertThat(result.sessionId()).isEqualTo(99L);
    }
}
