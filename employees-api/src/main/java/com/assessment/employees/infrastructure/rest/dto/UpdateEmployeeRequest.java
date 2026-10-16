package com.assessment.employees.infrastructure.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDate;

/**
 * Request DTO for partially updating an employee.
 * Omitted fields keep their current value; document and hireDate never change.
 */
@Schema(
    name = "UpdateEmployeeRequest",
    description = "Actualización parcial del empleado: solo los campos enviados se actualizan; "
        + "los omitidos conservan el valor actual.",
    example = "{\"position\":\"Senior Analyst\"}"
)
public record UpdateEmployeeRequest(
    @Schema(description = "Nuevo nombre; omitido mantiene el actual.", example = "Ana")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres.")
    String firstName,

    @Schema(description = "Nuevo apellido; omitido mantiene el actual.", example = "Diaz")
    @Size(max = 100, message = "El apellido no puede superar 100 caracteres.")
    String lastName,

    @Schema(description = "Nuevo correo; omitido mantiene el actual. Único por empleado.",
        example = "ana.diaz@example.com")
    @Email(message = "El correo electrónico no tiene un formato válido.")
    @Size(max = 255, message = "El correo electrónico no puede superar 255 caracteres.")
    String email,

    @Schema(description = "Nuevo cargo; omitido mantiene el actual.", example = "Senior Analyst")
    @Size(max = 100, message = "El cargo no puede superar 100 caracteres.")
    String position,

    @Schema(description = "Fecha de contratación inmutable: si se envía debe coincidir con la actual.",
        example = "2026-10-14")
    @PastOrPresent(message = "La fecha de contratación no puede ser futura.")
    LocalDate hireDate
) {}
