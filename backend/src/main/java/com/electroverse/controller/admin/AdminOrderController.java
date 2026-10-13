package com.electroverse.controller.admin;

import com.electroverse.dto.request.ShoppingRequests;
import com.electroverse.dto.response.CatalogResponses;
import com.electroverse.dto.response.ShoppingResponses;
import com.electroverse.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<CatalogResponses.Paged<ShoppingResponses.Order>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(orderService.adminList(PageRequest.of(page, Math.min(size, 60))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShoppingResponses.Order> get(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.adminGet(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ShoppingResponses.Order> updateStatus(
            @PathVariable Long id, @Valid @RequestBody ShoppingRequests.OrderStatusUpdate request) {
        return ResponseEntity.ok(orderService.adminUpdateStatus(id, request));
    }
}
