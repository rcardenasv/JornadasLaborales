package com.assessment.employees.domain.exception;

/**
 * Exception thrown when trying to create an employee with duplicate data.
 */
public class DuplicateEmployeeException extends DomainException {
    public DuplicateEmployeeException(String field, String value) {
        super("Ya existe un empleado con ese " + field + ".");
    }
}