package com.assessment.employees.infrastructure.rest.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDate;

/**
 * Response DTO for employee data.
 */
@Schema(name = "EmployeeResponse", description = "Datos del empleado; el UUID interno no se expone.")
public record EmployeeResponse(
    @Schema(description = "Tipo de documento: CC, CE o PA.", example = "CC")
    String documentType,

    @Schema(description = "Número de documento del empleado.", example = "123456789")
    String documentNumber,

    @Schema(description = "Nombre del empleado.", example = "Ana")
    String firstName,

    @Schema(description = "Apellido del empleado.", example = "Diaz")
    String lastName,

    @Schema(description = "Correo electrónico único del empleado.", example = "ana.diaz@example.com")
    String email,

    @Schema(description = "Cargo del empleado.", example = "Analyst")
    String position,

    @Schema(description = "Fecha de contratación; inmutable tras la creación.", example = "2026-10-14")
    LocalDate hireDate
) {}