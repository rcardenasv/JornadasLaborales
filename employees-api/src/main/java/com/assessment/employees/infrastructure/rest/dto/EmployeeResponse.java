package com.assessment.employees.infrastructure.rest.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Response DTO for employee data.
 */
public record EmployeeResponse(
    UUID id,
    String documentNumber,
    String firstName,
    String lastName,
    String email,
    String position,
    LocalDate hireDate
) {}