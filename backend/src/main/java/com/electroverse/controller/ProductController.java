package com.electroverse.controller;

import com.electroverse.dto.response.CatalogResponses;
import com.electroverse.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<CatalogResponses.Paged<CatalogResponses.ProductSummary>> list(
            @RequestParam Map<String, String> params) {
        Map<String, String> specs = new LinkedHashMap<>();
        params.forEach((k, v) -> {
            if (k.startsWith("spec_") && v != null && !v.isBlank()) {
                specs.put(k.substring(5), v);
            }
        });
        return ResponseEntity.ok(productService.search(
                params.get("q"),
                params.get("category"),
                params.get("brand"),
                parseDecimal(params.get("minPrice")),
                parseDecimal(params.get("maxPrice")),
                parseDouble(params.get("minRating")),
                parseBoolean(params.get("inStock")),
                parseBoolean(params.get("featured")),
                specs,
                params.get("sort"),
                parseInt(params.get("page"), 0),
                parseInt(params.get("size"), 12),
                false
        ));
    }

    @GetMapping("/featured")
    public ResponseEntity<List<CatalogResponses.ProductSummary>> featured() {
        return ResponseEntity.ok(productService.featured());
    }

    @GetMapping("/new-arrivals")
    public ResponseEntity<List<CatalogResponses.ProductSummary>> newArrivals() {
        return ResponseEntity.ok(productService.newArrivals());
    }

    @GetMapping("/deals")
    public ResponseEntity<List<CatalogResponses.ProductSummary>> deals() {
        return ResponseEntity.ok(productService.deals());
    }

    @GetMapping("/brands")
    public ResponseEntity<List<String>> brands() {
        return ResponseEntity.ok(productService.brands());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CatalogResponses.ProductDetail> detail(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getDetail(id));
    }

    @GetMapping("/{id}/specifications")
    public ResponseEntity<List<CatalogResponses.Spec>> specifications(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getSpecifications(id));
    }

    @GetMapping("/{id}/related")
    public ResponseEntity<List<CatalogResponses.ProductSummary>> related(@PathVariable Long id) {
        return ResponseEntity.ok(productService.related(id));
    }

    private BigDecimal parseDecimal(String v) {
        try {
            return v == null || v.isBlank() ? null : new BigDecimal(v);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Double parseDouble(String v) {
        try {
            return v == null || v.isBlank() ? null : Double.parseDouble(v);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Integer parseInt(String v, int fallback) {
        try {
            return v == null || v.isBlank() ? fallback : Integer.parseInt(v);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private Boolean parseBoolean(String v) {
        return v == null || v.isBlank() ? null : Boolean.parseBoolean(v);
    }
}
