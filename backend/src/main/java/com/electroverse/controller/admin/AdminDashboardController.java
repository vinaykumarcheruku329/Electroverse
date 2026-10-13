package com.electroverse.controller.admin;

import com.electroverse.dto.response.AdminResponses;
import com.electroverse.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
public class AdminDashboardController {

    private final AdminService adminService;

    public AdminDashboardController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public ResponseEntity<AdminResponses.Dashboard> dashboard() {
        return ResponseEntity.ok(adminService.dashboard());
    }
}
