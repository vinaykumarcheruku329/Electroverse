package com.electroverse.service;

import com.electroverse.dto.response.ShoppingResponses;
import com.electroverse.entity.Cart;
import com.electroverse.entity.CartItem;
import com.electroverse.entity.Product;
import com.electroverse.entity.User;
import com.electroverse.exception.BadRequestException;
import com.electroverse.exception.ResourceNotFoundException;
import com.electroverse.mapper.DtoMapper;
import com.electroverse.repository.CartItemRepository;
import com.electroverse.repository.CartRepository;
import com.electroverse.repository.ProductRepository;
import com.electroverse.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final SecurityUtils securityUtils;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       ProductRepository productRepository,
                       SecurityUtils securityUtils) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.securityUtils = securityUtils;
    }

    @Transactional
    public Cart getOrCreateCart(User user) {
        return cartRepository.findByUserId(user.getId()).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setUser(user);
            return cartRepository.save(cart);
        });
    }

    @Transactional
    public ShoppingResponses.Cart getCart() {
        User user = securityUtils.getCurrentUser();
        Cart cart = getOrCreateCart(user);
        return toResponse(cart);
    }

    @Transactional
    public ShoppingResponses.Cart addItem(Long productId, int quantity) {
        User user = securityUtils.getCurrentUser();
        Cart cart = getOrCreateCart(user);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
        if (!product.isActive()) {
            throw new BadRequestException("This product is not available");
        }
        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId).orElse(null);
        int newQuantity = quantity + (item != null ? item.getQuantity() : 0);
        if (product.getStock() < newQuantity) {
            throw new BadRequestException("Only " + product.getStock() + " unit(s) available in stock");
        }
        if (item == null) {
            item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
        }
        item.setQuantity(newQuantity);
        cartItemRepository.save(item);
        return toResponse(cart);
    }

    @Transactional
    public ShoppingResponses.Cart updateItem(Long itemId, int quantity) {
        User user = securityUtils.getCurrentUser();
        CartItem item = cartItemRepository.findByIdAndCartUserId(itemId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + itemId));
        Product product = item.getProduct();
        if (!product.isActive()) {
            throw new BadRequestException("This product is not available");
        }
        if (product.getStock() < quantity) {
            throw new BadRequestException("Only " + product.getStock() + " unit(s) available in stock");
        }
        item.setQuantity(quantity);
        cartItemRepository.save(item);
        return toResponse(getOrCreateCart(user));
    }

    @Transactional
    public ShoppingResponses.Cart removeItem(Long itemId) {
        User user = securityUtils.getCurrentUser();
        CartItem item = cartItemRepository.findByIdAndCartUserId(itemId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + itemId));
        cartItemRepository.delete(item);
        return toResponse(getOrCreateCart(user));
    }

    @Transactional
    public void clearCart(Cart cart) {
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    private ShoppingResponses.Cart toResponse(Cart cart) {
        List<CartItem> items = cartItemRepository.findByCartIdOrderByIdAsc(cart.getId());
        List<ShoppingResponses.CartItem> mapped = items.stream().map(DtoMapper::toCartItem).toList();
        BigDecimal subtotal = mapped.stream()
                .map(ShoppingResponses.CartItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int count = mapped.stream().mapToInt(ShoppingResponses.CartItem::quantity).sum();
        return new ShoppingResponses.Cart(cart.getId(), mapped, count, subtotal);
    }
}
