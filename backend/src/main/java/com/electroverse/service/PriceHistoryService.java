package com.electroverse.service;

import com.electroverse.dto.request.FeatureRequests;
import com.electroverse.dto.response.FeatureResponses;
import com.electroverse.entity.PriceAlert;
import com.electroverse.entity.PriceHistory;
import com.electroverse.entity.Product;
import com.electroverse.exception.ResourceNotFoundException;
import com.electroverse.repository.PriceAlertRepository;
import com.electroverse.repository.PriceHistoryRepository;
import com.electroverse.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class PriceHistoryService {

    private final PriceHistoryRepository priceHistoryRepository;
    private final ProductRepository productRepository;
    private final PriceAlertRepository priceAlertRepository;

    public PriceHistoryService(PriceHistoryRepository priceHistoryRepository,
                               ProductRepository productRepository,
                               PriceAlertRepository priceAlertRepository) {
        this.priceHistoryRepository = priceHistoryRepository;
        this.productRepository = productRepository;
        this.priceAlertRepository = priceAlertRepository;
    }

    @Transactional(readOnly = true)
    public FeatureResponses.PriceHistory getHistory(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
        List<PriceHistory> history = priceHistoryRepository.findByProductIdOrderByRecordedAtAsc(productId);
        List<FeatureResponses.PricePoint> points = history.stream()
                .map(h -> new FeatureResponses.PricePoint(h.getRecordedAt(), h.getPrice(), h.getSource(), h.isDemoData()))
                .toList();
        BigDecimal lowest = history.stream().map(PriceHistory::getPrice).min(Comparator.naturalOrder()).orElse(null);
        PriceHistory latest = history.isEmpty() ? null : history.get(history.size() - 1);
        return new FeatureResponses.PriceHistory(
                product.getId(),
                points,
                lowest != null ? lowest : product.getPrice(),
                latest != null ? latest.getPrice() : product.getPrice(),
                latest != null ? latest.getRecordedAt() : product.getCreatedAt(),
                latest != null ? latest.getSource() : "catalog",
                latest == null || latest.isDemoData());
    }

    @Transactional
    public FeatureResponses.PricePoint addSnapshot(Long productId, FeatureRequests.PriceSnapshot request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
        PriceHistory snapshot = new PriceHistory();
        snapshot.setProduct(product);
        snapshot.setPrice(request.price());
        snapshot.setRecordedAt(request.recordedAt() != null ? request.recordedAt() : LocalDateTime.now());
        snapshot.setSource(request.source() != null && !request.source().isBlank() ? request.source() : "admin-entry");
        snapshot.setDemoData(request.demoData() == null || request.demoData());
        PriceHistory saved = priceHistoryRepository.save(snapshot);
        triggerAlerts(product, request.price());
        return new FeatureResponses.PricePoint(saved.getRecordedAt(), saved.getPrice(), saved.getSource(), saved.isDemoData());
    }

    private void triggerAlerts(Product product, BigDecimal price) {
        List<PriceAlert> alerts = priceAlertRepository.findByProductIdAndActiveTrueAndTriggeredFalse(product.getId());
        for (PriceAlert alert : alerts) {
            alert.setLastObservedPrice(price);
            if (price.compareTo(alert.getTargetPrice()) <= 0) {
                alert.setTriggered(true);
                alert.setTriggeredAt(LocalDateTime.now());
            }
            priceAlertRepository.save(alert);
        }
    }

    @Transactional(readOnly = true)
    public List<FeatureResponses.PricePoint> recent() {
        return priceHistoryRepository.findTop20ByOrderByRecordedAtDesc().stream()
                .map(h -> new FeatureResponses.PricePoint(h.getRecordedAt(), h.getPrice(), h.getSource(), h.isDemoData()))
                .toList();
    }
}
