package com.electroverse.service;

import com.electroverse.dto.response.CatalogResponses;
import com.electroverse.entity.Product;
import com.electroverse.entity.User;
import com.electroverse.entity.WishlistItem;
import com.electroverse.exception.ConflictException;
import com.electroverse.exception.ResourceNotFoundException;
import com.electroverse.mapper.DtoMapper;
import com.electroverse.repository.ProductRepository;
import com.electroverse.repository.WishlistItemRepository;
import com.electroverse.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WishlistService {

    private final WishlistItemRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final SecurityUtils securityUtils;

    public WishlistService(WishlistItemRepository wishlistRepository,
                           ProductRepository productRepository,
                           SecurityUtils securityUtils) {
        this.wishlistRepository = wishlistRepository;
        this.productRepository = productRepository;
        this.securityUtils = securityUtils;
    }

    @Transactional(readOnly = true)
    public List<CatalogResponses.ProductSummary> listMine() {
        User user = securityUtils.getCurrentUser();
        return wishlistRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(WishlistItem::getProduct)
                .map(DtoMapper::toProductSummary)
                .toList();
    }

    @Transactional
    public void add(Long productId) {
        User user = securityUtils.getCurrentUser();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
        if (!product.isActive()) {
            throw new ResourceNotFoundException("Product not found: " + productId);
        }
        if (wishlistRepository.existsByUserIdAndProductId(user.getId(), productId)) {
            throw new ConflictException("Product is already in your wishlist");
        }
        WishlistItem item = new WishlistItem();
        item.setUser(user);
        item.setProduct(product);
        wishlistRepository.save(item);
    }

    @Transactional
    public void remove(Long productId) {
        User user = securityUtils.getCurrentUser();
        WishlistItem item = wishlistRepository.findByUserIdAndProductId(user.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product is not in your wishlist"));
        wishlistRepository.delete(item);
    }
}
