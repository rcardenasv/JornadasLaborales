package com.assessment.employees.domain.exception;

/**
 * Indica que los parámetros de paginación no son válidos.
 */
public class InvalidPaginationException extends DomainException {
    public InvalidPaginationException(String message) {
        super(message);
    }
}
