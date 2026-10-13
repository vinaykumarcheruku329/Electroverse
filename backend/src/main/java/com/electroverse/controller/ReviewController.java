package com.electroverse.controller;

import com.electroverse.dto.request.ShoppingRequests;
import com.electroverse.dto.response.CatalogResponses;
import com.electroverse.dto.response.ShoppingResponses;
import com.electroverse.service.ReviewService;
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
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<CatalogResponses.Paged<ShoppingResponses.Review>> list(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(reviewService.listByProduct(productId, PageRequest.of(page, Math.min(size, 50))));
    }

    @PostMapping("/products/{productId}/reviews")
    public ResponseEntity<ShoppingResponses.Review> create(@PathVariable Long productId,
                                                           @Valid @RequestBody ShoppingRequests.Review request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.create(productId, request));
    }
}
