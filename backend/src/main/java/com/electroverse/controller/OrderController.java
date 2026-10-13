package com.electroverse.controller;

import com.electroverse.dto.request.ShoppingRequests;
import com.electroverse.dto.response.CatalogResponses;
import com.electroverse.dto.response.ShoppingResponses;
import com.electroverse.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<ShoppingResponses.Order> checkout(@Valid @RequestBody ShoppingRequests.Checkout request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.checkout(request));
    }

    @GetMapping("/orders")
    public ResponseEntity<CatalogResponses.Paged<ShoppingResponses.Order>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(orderService.listMine(PageRequest.of(page, Math.min(size, 50))));
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<ShoppingResponses.Order> get(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getMine(id));
    }

    @PostMapping("/orders/{id}/cancel")
    public ResponseEntity<ShoppingResponses.Order> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancel(id));
    }
}
