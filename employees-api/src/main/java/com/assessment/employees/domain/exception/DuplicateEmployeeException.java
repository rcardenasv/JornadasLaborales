package com.assessment.employees.domain.exception;

/**
 * Indica que ya existe un empleado con el correo o el documento recibido.
 */
public class DuplicateEmployeeException extends DomainException {
    public DuplicateEmployeeException(String field) {
        super("Ya existe un empleado con ese " + field + ".");
    }
}