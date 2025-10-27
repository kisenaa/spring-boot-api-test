package org.elysia.api.exception;
import java.time.LocalDateTime;

public record ErrorResponse(String errorName, String message, int status, LocalDateTime timestamp) {
    public ErrorResponse(String errorName, String message, int status) {
        this(errorName, message, status, LocalDateTime.now());
    }
}