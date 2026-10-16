package com.assessment.employees.application.dto;

import java.time.LocalDate;

/**
 * Command to update an existing employee.
 */
public record UpdateEmployeeCommand(
    String id,
    String documentNumber,
    String firstName,
    String lastName,
    String email,
    String position,
    LocalDate hireDate
) {
}