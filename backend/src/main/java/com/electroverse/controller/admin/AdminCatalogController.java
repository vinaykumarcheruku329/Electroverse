package com.electroverse.controller.admin;

import com.electroverse.dto.request.CatalogRequests;
import com.electroverse.dto.response.AdminResponses;
import com.electroverse.dto.response.CatalogResponses;
import com.electroverse.service.AdminService;
import com.electroverse.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminCatalogController {

    private final CategoryService categoryService;
    private final AdminService adminService;

    public AdminCatalogController(CategoryService categoryService, AdminService adminService) {
        this.categoryService = categoryService;
        this.adminService = adminService;
    }

    @PostMapping("/categories")
    public ResponseEntity<CatalogResponses.Category> createCategory(
            @Valid @RequestBody CatalogRequests.Category request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(request));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<CatalogResponses.Category> updateCategory(
            @PathVariable Long id, @Valid @RequestBody CatalogRequests.Category request) {
        return ResponseEntity.ok(categoryService.update(id, request));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/inventory")
    public ResponseEntity<List<AdminResponses.InventoryRow>> inventory(
            @RequestParam(defaultValue = "false") boolean lowStockOnly) {
        return ResponseEntity.ok(adminService.inventory(lowStockOnly));
    }
}
