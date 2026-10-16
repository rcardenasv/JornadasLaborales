package com.assessment.employees.domain.exception;

/**
 * Indica que la jornada laboral no cumple sus invariantes
 * (campos obligatorios u horario inválido). Se traduce en HTTP 400.
 */
public class InvalidWorkShiftException extends DomainException {
    public InvalidWorkShiftException(String message) {
        super(message);
    }
}
