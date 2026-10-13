package com.electroverse.controller.admin;

import com.electroverse.dto.response.AdminResponses;
import com.electroverse.entity.enums.ReviewStatus;
import com.electroverse.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/reviews")
public class AdminReviewController {

    private final ReviewService reviewService;

    public AdminReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public ResponseEntity<List<AdminResponses.ReviewModeration>> list() {
        return ResponseEntity.ok(reviewService.adminList());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AdminResponses.ReviewModeration> moderate(
            @PathVariable Long id, @RequestParam ReviewStatus status) {
        return ResponseEntity.ok(reviewService.moderate(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reviewService.delete(id, true);
        return ResponseEntity.noContent().build();
    }
}
