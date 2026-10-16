package com.assessment.employees.application.dto;

import com.assessment.employees.domain.model.Document;

import java.time.LocalDate;

/**
 * Command to update an existing employee. Los campos null conservan su valor.
 */
public record UpdateEmployeeCommand(
    Document document,
    String firstName,
    String lastName,
    String email,
    String position,
    LocalDate hireDate
) {
}
