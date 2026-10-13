package com.electroverse.controller.admin;

import com.electroverse.dto.request.FeatureRequests;
import com.electroverse.dto.response.FeatureResponses;
import com.electroverse.service.CompatibilityService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/compatibility-rules")
public class AdminCompatibilityController {

    private final CompatibilityService compatibilityService;

    public AdminCompatibilityController(CompatibilityService compatibilityService) {
        this.compatibilityService = compatibilityService;
    }

    @GetMapping
    public ResponseEntity<List<FeatureResponses.CompatibilityRule>> list() {
        return ResponseEntity.ok(compatibilityService.listRules());
    }

    @PostMapping
    public ResponseEntity<FeatureResponses.CompatibilityRule> create(
            @Valid @RequestBody FeatureRequests.CompatibilityRule request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(compatibilityService.createRule(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FeatureResponses.CompatibilityRule> update(
            @PathVariable Long id, @Valid @RequestBody FeatureRequests.CompatibilityRule request) {
        return ResponseEntity.ok(compatibilityService.updateRule(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        compatibilityService.deleteRule(id);
        return ResponseEntity.noContent().build();
    }
}
