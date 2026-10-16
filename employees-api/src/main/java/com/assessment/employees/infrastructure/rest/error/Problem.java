package com.assessment.employees.infrastructure.rest.error;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Problem Details for HTTP APIs (RFC 7807).
 * Solo incluye los campos previstos en el contrato; `errors` aparece únicamente
 * en errores de validación.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Problem(
    String type,
    String title,
    int status,
    String detail,
    String instance,
    Object errors
) {
    public static Problem validationError(String message, Object errors) {
        return new Problem(
            "https://tools.ietf.org/html/rfc7807#section-3.1",
            "Error de validación",
            400,
            message,
            null,
            errors
        );
    }

    public static Problem notFound(String message) {
        return new Problem(
            "https://tools.ietf.org/html/rfc7807#section-3.1",
            "No encontrado",
            404,
            message,
            null,
            null
        );
    }

    public static Problem conflict(String message) {
        return new Problem(
            "https://tools.ietf.org/html/rfc7807#section-3.1",
            "Conflicto",
            409,
            message,
            null,
            null
        );
    }

    public static Problem internalError(String message) {
        return new Problem(
            "https://tools.ietf.org/html/rfc7807#section-3.1",
            "Error interno del servidor",
            500,
            message,
            null,
            null
        );
    }

    public static Problem badRequest(String message) {
        return validationError(message, null);
    }

    public Problem withInstance(String requestPath) {
        return new Problem(type, title, status, detail, requestPath, errors);
    }
}
