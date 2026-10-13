package com.electroverse.controller;

import com.electroverse.dto.response.FeatureResponses;
import com.electroverse.service.LifecycleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lifecycle")
public class LifecycleController {

    private final LifecycleService lifecycleService;

    public LifecycleController(LifecycleService lifecycleService) {
        this.lifecycleService = lifecycleService;
    }

    @GetMapping("/{productId}")
    public ResponseEntity<FeatureResponses.Lifecycle> latest(@PathVariable Long productId) {
        return lifecycleService.getLatest(productId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/{productId}/history")
    public ResponseEntity<List<FeatureResponses.Lifecycle>> history(@PathVariable Long productId) {
        return ResponseEntity.ok(lifecycleService.history(productId));
    }
}
