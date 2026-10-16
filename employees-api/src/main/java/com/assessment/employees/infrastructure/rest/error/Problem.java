package com.assessment.employees.infrastructure.rest.error;

import java.time.Instant;

/**
 * Problem Details for HTTP APIs (RFC 7807).
 */
public record Problem(
    String type,
    String title,
    int status,
    String detail,
    String instance,
    Object errors,
    Instant timestamp
) {
    public static Problem validationError(String message, Object errors) {
        return new Problem(
            "https://tools.ietf.org/html/rfc7807#section-3.1",
            "Error de validación",
            400,
            message,
            null,
            errors,
            Instant.now()
        );
    }
    
    public static Problem notFound(String message) {
        return new Problem(
            "https://tools.ietf.org/html/rfc7807#section-3.1",
            "No encontrado",
            404,
            message,
            null,
            null,
            Instant.now()
        );
    }
    
    public static Problem conflict(String message) {
        return new Problem(
            "https://tools.ietf.org/html/rfc7807#section-3.1",
            "Conflicto",
            409,
            message,
            null,
            null,
            Instant.now()
        );
    }
    
    public static Problem internalError(String message) {
        return new Problem(
            "https://tools.ietf.org/html/rfc7807#section-3.1",
            "Error interno del servidor",
            500,
            message,
            null,
            null,
            Instant.now()
        );
    }

    public static Problem badRequest(String message) {
        return validationError(message, null);
    }

    public Problem withInstance(String requestPath) {
        return new Problem(type, title, status, detail, requestPath, errors, timestamp);
    }
}