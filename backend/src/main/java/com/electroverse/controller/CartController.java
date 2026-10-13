package com.electroverse.controller;

import com.electroverse.dto.request.ShoppingRequests;
import com.electroverse.dto.response.ShoppingResponses;
import com.electroverse.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<ShoppingResponses.Cart> get() {
        return ResponseEntity.ok(cartService.getCart());
    }

    @PostMapping("/items")
    public ResponseEntity<ShoppingResponses.Cart> addItem(@Valid @RequestBody ShoppingRequests.CartItem request) {
        return ResponseEntity.ok(cartService.addItem(request.productId(), request.quantity()));
    }

    @PatchMapping("/items/{itemId}")
    public ResponseEntity<ShoppingResponses.Cart> updateItem(@PathVariable Long itemId,
                                                             @Valid @RequestBody ShoppingRequests.CartUpdate request) {
        return ResponseEntity.ok(cartService.updateItem(itemId, request.quantity()));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ShoppingResponses.Cart> removeItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(cartService.removeItem(itemId));
    }
}
