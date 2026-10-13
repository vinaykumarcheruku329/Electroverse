package com.electroverse.controller.admin;

import com.electroverse.dto.request.FeatureRequests;
import com.electroverse.dto.response.FeatureResponses;
import com.electroverse.service.LifecycleService;
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
@RequestMapping("/api/v1/admin/lifecycle")
public class AdminLifecycleController {

    private final LifecycleService lifecycleService;

    public AdminLifecycleController(LifecycleService lifecycleService) {
        this.lifecycleService = lifecycleService;
    }

    @GetMapping
    public ResponseEntity<List<FeatureResponses.Lifecycle>> recent() {
        return ResponseEntity.ok(lifecycleService.recent());
    }

    @PostMapping
    public ResponseEntity<FeatureResponses.Lifecycle> create(
            @Valid @RequestBody FeatureRequests.Lifecycle request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lifecycleService.create(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        lifecycleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
