package com.electroverse.controller;

import com.electroverse.dto.request.ShoppingRequests;
import com.electroverse.dto.response.ShoppingResponses;
import com.electroverse.service.PriceAlertService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/price-alerts")
public class PriceAlertController {

    private final PriceAlertService priceAlertService;

    public PriceAlertController(PriceAlertService priceAlertService) {
        this.priceAlertService = priceAlertService;
    }

    @GetMapping
    public ResponseEntity<List<ShoppingResponses.PriceAlert>> list() {
        return ResponseEntity.ok(priceAlertService.listMine());
    }

    @PostMapping
    public ResponseEntity<ShoppingResponses.PriceAlert> create(@Valid @RequestBody ShoppingRequests.PriceAlert request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(priceAlertService.create(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        priceAlertService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
