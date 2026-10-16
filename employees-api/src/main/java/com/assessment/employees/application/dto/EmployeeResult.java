package com.assessment.employees.application.dto;

import java.time.LocalDate;

/**
 * Result object for employee operations.
 */
public record EmployeeResult(
    String documentType,
    String documentNumber,
    String firstName,
    String lastName,
    String email,
    String position,
    LocalDate hireDate
) {
    public static EmployeeResult fromDomain(com.assessment.employees.domain.model.Employee employee) {
        return new EmployeeResult(
            employee.getDocumentType().name(),
            employee.getDocumentNumber(),
            employee.getFirstName(),
            employee.getLastName(),
            employee.getEmail().value(),
            employee.getPosition(),
            employee.getHireDate()
        );
    }
}