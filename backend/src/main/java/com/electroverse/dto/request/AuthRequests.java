package com.electroverse.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class AuthRequests {

    private AuthRequests() {
    }

    public record Register(
            @NotBlank(message = "Email is required")
            @Email(message = "Email must be valid")
            @Size(max = 180, message = "Email is too long")
            String email,

            @NotBlank(message = "Password is required")
            @Size(min = 8, max = 100, message = "Password must be at least 8 characters")
            String password,

            @NotBlank(message = "Full name is required")
            @Size(max = 120, message = "Full name is too long")
            String fullName,

            @Size(max = 30, message = "Phone is too long")
            @Pattern(regexp = "^$|^(?:\\+?91[\\s-]?|0)?[6-9]\\d{9}$",
                    message = "Enter a valid Indian mobile number")
            String phone
    ) {
    }

    public record Login(
            @NotBlank(message = "Email is required")
            @Email(message = "Email must be valid")
            String email,

            @NotBlank(message = "Password is required")
            String password
    ) {
    }

    public record UpdateProfile(
            @NotBlank(message = "Full name is required")
            @Size(max = 120, message = "Full name is too long")
            String fullName,

            @Size(max = 30, message = "Phone is too long")
            String phone
    ) {
    }
}
