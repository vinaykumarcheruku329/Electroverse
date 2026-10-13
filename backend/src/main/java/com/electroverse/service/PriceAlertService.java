package com.electroverse.service;

import com.electroverse.dto.request.ShoppingRequests;
import com.electroverse.dto.response.ShoppingResponses;
import com.electroverse.entity.PriceAlert;
import com.electroverse.entity.Product;
import com.electroverse.entity.User;
import com.electroverse.exception.ConflictException;
import com.electroverse.exception.ResourceNotFoundException;
import com.electroverse.mapper.DtoMapper;
import com.electroverse.repository.PriceAlertRepository;
import com.electroverse.repository.ProductRepository;
import com.electroverse.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PriceAlertService {

    private final PriceAlertRepository alertRepository;
    private final ProductRepository productRepository;
    private final SecurityUtils securityUtils;

    public PriceAlertService(PriceAlertRepository alertRepository,
                             ProductRepository productRepository,
                             SecurityUtils securityUtils) {
        this.alertRepository = alertRepository;
        this.productRepository = productRepository;
        this.securityUtils = securityUtils;
    }

    @Transactional(readOnly = true)
    public List<ShoppingResponses.PriceAlert> listMine() {
        User user = securityUtils.getCurrentUser();
        return alertRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(DtoMapper::toPriceAlert).toList();
    }

    @Transactional
    public ShoppingResponses.PriceAlert create(ShoppingRequests.PriceAlert request) {
        User user = securityUtils.getCurrentUser();
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + request.productId()));
        if (alertRepository.existsByUserIdAndProductId(user.getId(), product.getId())) {
            throw new ConflictException("You already have a price alert for this product");
        }
        PriceAlert alert = new PriceAlert();
        alert.setUser(user);
        alert.setProduct(product);
        alert.setTargetPrice(request.targetPrice());
        alert.setActive(true);
        alert.setTriggered(product.getPrice().compareTo(request.targetPrice()) <= 0);
        if (alert.isTriggered()) {
            alert.setTriggeredAt(java.time.LocalDateTime.now());
        }
        alert.setLastObservedPrice(product.getPrice());
        return DtoMapper.toPriceAlert(alertRepository.save(alert));
    }

    @Transactional
    public void delete(Long id) {
        User user = securityUtils.getCurrentUser();
        PriceAlert alert = alertRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Price alert not found: " + id));
        alertRepository.delete(alert);
    }
}
