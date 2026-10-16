package com.assessment.employees.domain.exception;

import com.assessment.employees.domain.model.Document;
import com.assessment.employees.domain.model.EmployeeId;

/**
 * Exception thrown when an employee is not found.
 */
public class EmployeeNotFoundException extends DomainException {
    public EmployeeNotFoundException(Document document) {
        super("No se encontró el empleado con el documento solicitado.");
    }

    public EmployeeNotFoundException(EmployeeId id) {
        super("No se encontró el empleado solicitado.");
    }
}