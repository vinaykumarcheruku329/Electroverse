package com.electroverse.controller;

import com.electroverse.dto.request.FeatureRequests;
import com.electroverse.dto.response.FeatureResponses;
import com.electroverse.service.ComparisonService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/comparisons")
public class ComparisonController {

    private final ComparisonService comparisonService;

    public ComparisonController(ComparisonService comparisonService) {
        this.comparisonService = comparisonService;
    }

    @PostMapping
    public ResponseEntity<FeatureResponses.Comparison> compare(@Valid @RequestBody FeatureRequests.Comparison request) {
        return ResponseEntity.ok(comparisonService.compare(request));
    }
}
