package com.electroverse.exception;

import java.util.List;

public record ApiErrorResponse(
        java.time.LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldViolation> fieldErrors
) {
    public record FieldViolation(String field, String message) {
    }
}
