package com.electroverse.controller;

import com.electroverse.dto.response.FeatureResponses;
import com.electroverse.service.PriceHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
public class PriceHistoryController {

    private final PriceHistoryService priceHistoryService;

    public PriceHistoryController(PriceHistoryService priceHistoryService) {
        this.priceHistoryService = priceHistoryService;
    }

    @GetMapping("/{id}/price-history")
    public ResponseEntity<FeatureResponses.PriceHistory> history(@PathVariable Long id) {
        return ResponseEntity.ok(priceHistoryService.getHistory(id));
    }
}
