package com.electroverse.dto.response;

import java.time.LocalDateTime;

public final class AuthResponses {

    private AuthResponses() {
    }

    public record User(
            Long id,
            String email,
            String fullName,
            String phone,
            String role,
            boolean active,
            LocalDateTime createdAt
    ) {
    }

    public record Auth(
            String token,
            String tokenType,
            long expiresInMs,
            User user
    ) {
    }
}
