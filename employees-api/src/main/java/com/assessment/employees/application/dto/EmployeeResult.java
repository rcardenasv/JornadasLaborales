package com.assessment.employees.application.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Result object for employee operations.
 */
public record EmployeeResult(
    UUID id,
    String documentNumber,
    String firstName,
    String lastName,
    String email,
    String position,
    LocalDate hireDate
) {
    public static EmployeeResult fromDomain(com.assessment.employees.domain.model.Employee employee) {
        return new EmployeeResult(
            employee.getId().value(),
            employee.getDocumentNumber().value(),
            employee.getFirstName(),
            employee.getLastName(),
            employee.getEmail().value(),
            employee.getPosition(),
            employee.getHireDate()
        );
    }
}