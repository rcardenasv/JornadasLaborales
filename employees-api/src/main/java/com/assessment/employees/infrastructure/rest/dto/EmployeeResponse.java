package com.assessment.employees.infrastructure.rest.dto;

import java.time.LocalDate;

/**
 * Response DTO for employee data.
 */
public record EmployeeResponse(
    String documentType,
    String documentNumber,
    String firstName,
    String lastName,
    String email,
    String position,
    LocalDate hireDate
) {}