package com.assessment.employees.infrastructure.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDate;

/**
 * Request DTO for creating an employee.
 */
@Schema(name = "CreateEmployeeRequest", description = "Datos requeridos para crear un empleado.")
public record CreateEmployeeRequest(
    @Schema(description = "Tipo de documento: CC, CE o PA.", example = "CC")
    @NotBlank(message = "El tipo de documento es obligatorio.")
    String documentType,

    @Schema(description = "Número de documento; CC/CE solo dígitos (3–20), PA alfanumérico (5–20). "
        + "Inmutable tras la creación.",
        example = "123456789")
    @NotBlank(message = "El número de documento es obligatorio.")
    @Size(min = 3, max = 20, message = "El número de documento debe tener entre 3 y 20 caracteres.")
    @Pattern(regexp = "[a-zA-Z0-9]+", message = "El número de documento debe ser alfanumérico.")
    String documentNumber,
    
    @Schema(description = "Nombre del empleado (1–100 caracteres).", example = "Ana")
    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres.")
    String firstName,
    
    @Schema(description = "Apellido del empleado (1–100 caracteres).", example = "Diaz")
    @NotBlank(message = "El apellido es obligatorio.")
    @Size(max = 100, message = "El apellido no puede superar 100 caracteres.")
    String lastName,
    
    @Schema(description = "Correo electrónico único del empleado.", example = "ana.diaz@example.com")
    @NotBlank(message = "El correo electrónico es obligatorio.")
    @Email(message = "El correo electrónico no tiene un formato válido.")
    @Size(max = 255, message = "El correo electrónico no puede superar 255 caracteres.")
    String email,
    
    @Schema(description = "Cargo del empleado (1–100 caracteres).", example = "Analyst")
    @NotBlank(message = "El cargo es obligatorio.")
    @Size(max = 100, message = "El cargo no puede superar 100 caracteres.")
    String position,
    
    @Schema(description = "Fecha de contratación; no puede ser futura e inmutable tras la creación.",
        example = "2026-10-14")
    @NotNull(message = "La fecha de contratación es obligatoria.")
    @PastOrPresent(message = "La fecha de contratación no puede ser futura.")
    LocalDate hireDate
) {}