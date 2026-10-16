package com.assessment.employees.application.dto;

import com.assessment.employees.domain.model.Document;

import java.time.LocalDate;

/**
 * Command to create a new employee.
 */
public record CreateEmployeeCommand(
    Document document,
    String firstName,
    String lastName,
    String email,
    String position,
    LocalDate hireDate
) {
}
