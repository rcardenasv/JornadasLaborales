package com.assessment.employees.application.dto;

import java.time.LocalDate;

/**
 * Command to create a new employee.
 */
public record CreateEmployeeCommand(
    String documentNumber,
    String firstName,
    String lastName,
    String email,
    String position,
    LocalDate hireDate
) {
}