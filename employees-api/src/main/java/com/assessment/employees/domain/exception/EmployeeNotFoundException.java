package com.assessment.employees.domain.exception;

import com.assessment.employees.domain.model.EmployeeId;

/**
 * Exception thrown when an employee is not found.
 */
public class EmployeeNotFoundException extends DomainException {
    public EmployeeNotFoundException(EmployeeId id) {
        super("Employee with id " + id + " not found");
    }
}