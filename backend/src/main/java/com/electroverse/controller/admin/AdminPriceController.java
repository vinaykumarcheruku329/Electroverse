package com.electroverse.controller.admin;

import com.electroverse.dto.request.FeatureRequests;
import com.electroverse.dto.response.FeatureResponses;
import com.electroverse.service.PriceHistoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminPriceController {

    private final PriceHistoryService priceHistoryService;

    public AdminPriceController(PriceHistoryService priceHistoryService) {
        this.priceHistoryService = priceHistoryService;
    }

    @PostMapping("/products/{id}/price-snapshots")
    public ResponseEntity<FeatureResponses.PricePoint> addSnapshot(
            @PathVariable Long id, @Valid @RequestBody FeatureRequests.PriceSnapshot request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(priceHistoryService.addSnapshot(id, request));
    }

    @GetMapping("/price-snapshots/recent")
    public ResponseEntity<List<FeatureResponses.PricePoint>> recent() {
        return ResponseEntity.ok(priceHistoryService.recent());
    }
}
