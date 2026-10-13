package com.electroverse.controller.admin;

import com.electroverse.dto.request.CatalogRequests;
import com.electroverse.dto.response.CatalogResponses;
import com.electroverse.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/products")
public class AdminProductController {

    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<CatalogResponses.Paged<CatalogResponses.ProductSummary>> list(
            @RequestParam Map<String, String> params) {
        return ResponseEntity.ok(productService.search(
                params.get("q"),
                params.get("category"),
                params.get("brand"),
                null, null, null, null, null,
                java.util.Map.of(),
                params.get("sort"),
                parseInt(params.get("page"), 0),
                parseInt(params.get("size"), 20),
                true
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CatalogResponses.ProductDetail> get(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getAdminDetail(id));
    }

    @PostMapping
    public ResponseEntity<CatalogResponses.ProductDetail> create(
            @Valid @RequestBody CatalogRequests.Product request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CatalogResponses.ProductDetail> update(
            @PathVariable Long id, @Valid @RequestBody CatalogRequests.Product request) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<CatalogResponses.ProductDetail> updateStock(
            @PathVariable Long id, @Valid @RequestBody CatalogRequests.StockUpdate request) {
        return ResponseEntity.ok(productService.updateStock(id, request.stock()));
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<CatalogResponses.ProductDetail> setActive(
            @PathVariable Long id, @RequestParam boolean active) {
        return ResponseEntity.ok(productService.setActive(id, active));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CatalogResponses.ProductDetail> deactivate(@PathVariable Long id) {
        return ResponseEntity.ok(productService.setActive(id, false));
    }

    private int parseInt(String v, int fallback) {
        try {
            return v == null || v.isBlank() ? fallback : Integer.parseInt(v);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }
}
