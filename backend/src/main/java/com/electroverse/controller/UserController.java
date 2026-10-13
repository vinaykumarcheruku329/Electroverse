package com.electroverse.controller;

import com.electroverse.dto.request.AuthRequests;
import com.electroverse.dto.response.AuthResponses;
import com.electroverse.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponses.User> me() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    @PutMapping("/me")
    public ResponseEntity<AuthResponses.User> update(@Valid @RequestBody AuthRequests.UpdateProfile request) {
        return ResponseEntity.ok(userService.updateProfile(request));
    }
}
