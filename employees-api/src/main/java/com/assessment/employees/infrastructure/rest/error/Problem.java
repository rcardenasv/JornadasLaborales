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
    private static final String RFC7807_TYPE = "https://tools.ietf.org/html/rfc7807#section-3.1";

    public static Problem validationError(String message, Object errors) {
        return new Problem(
            RFC7807_TYPE,
            "Error de validación",
            400,
            message,
            null,
            errors
        );
    }

    public static Problem notFound(String message) {
        return new Problem(
            RFC7807_TYPE,
            "No encontrado",
            404,
            message,
            null,
            null
        );
    }

    public static Problem conflict(String message) {
        return new Problem(
            RFC7807_TYPE,
            "Conflicto",
            409,
            message,
            null,
            null
        );
    }

    public static Problem internalError(String message) {
        return new Problem(
            RFC7807_TYPE,
            "Error interno del servidor",
            500,
            message,
            null,
            null
        );
    }

    public static Problem badRequest(String message) {
        return new Problem(
            RFC7807_TYPE,
            "Solicitud inválida",
            400,
            message,
            null,
            null
        );
    }

    public Problem withInstance(String requestPath) {
        return new Problem(type, title, status, detail, requestPath, errors);
    }
}
