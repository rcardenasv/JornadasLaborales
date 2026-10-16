package com.assessment.employees.infrastructure.rest.dto;

import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Request DTO for updating a work shift.
 */
@Schema(name = "UpdateWorkShiftRequest", description = "Nuevos datos completos de la jornada laboral.")
public record UpdateWorkShiftRequest(
    @Schema(example = "2026-10-15")
    @NotNull(message = "La fecha de la jornada es obligatoria.")
    LocalDate date,
    
    @Schema(example = "08:30:00")
    @NotNull(message = "La hora de inicio es obligatoria.")
    LocalTime startTime,
    
    @Schema(example = "16:30:00")
    @NotNull(message = "La hora de fin es obligatoria.")
    LocalTime endTime
) {}