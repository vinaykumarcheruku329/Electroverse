package com.electroverse.controller.admin;

import com.electroverse.dto.request.ShoppingRequests;
import com.electroverse.dto.response.AuthResponses;
import com.electroverse.dto.response.CatalogResponses;
import com.electroverse.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private final AdminService adminService;

    public AdminUserController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public ResponseEntity<CatalogResponses.Paged<AuthResponses.User>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.listUsers(page, size));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AuthResponses.User> setActive(@PathVariable Long id,
                                                        @Valid @RequestBody ShoppingRequests.UserStatusUpdate request) {
        return ResponseEntity.ok(adminService.setUserActive(id, request.active()));
    }
}
