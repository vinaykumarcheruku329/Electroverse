package com.electroverse.controller;

import com.electroverse.dto.request.FeatureRequests;
import com.electroverse.dto.response.FeatureResponses;
import com.electroverse.service.FinderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/finder")
public class FinderController {

    private final FinderService finderService;

    public FinderController(FinderService finderService) {
        this.finderService = finderService;
    }

    @PostMapping("/recommendations")
    public ResponseEntity<FeatureResponses.Finder> recommend(@Valid @RequestBody FeatureRequests.Finder request) {
        return ResponseEntity.ok(finderService.recommend(request));
    }

    @GetMapping("/sessions/{id}")
    public ResponseEntity<FeatureResponses.Finder> session(@PathVariable Long id) {
        return ResponseEntity.ok(finderService.getSession(id));
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<FeatureResponses.FinderSessionSummary>> sessions() {
        return ResponseEntity.ok(finderService.mySessions());
    }

    @GetMapping("/criteria")
    public ResponseEntity<List<String>> criteria() {
        return ResponseEntity.ok(FinderService.CRITERIA);
    }
}
