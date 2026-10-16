package com.assessment.employees.infrastructure.rest.dto;

import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Request DTO for updating a work shift (full replacement). Times use the HH:mm contract.
 */
@Schema(
    name = "UpdateWorkShiftRequest",
    description = "Nuevos datos completos de la jornada laboral.",
    example = "{\"date\":\"2026-10-16\",\"startTime\":\"08:00\",\"endTime\":\"17:00\"}"
)
public record UpdateWorkShiftRequest(
    @Schema(description = "Fecha de inicio de la jornada.", example = "2026-10-16")
    @NotNull(message = "La fecha de la jornada es obligatoria.")
    LocalDate date,

    @Schema(description = "Hora de inicio.", example = "08:00")
    @JsonFormat(pattern = "HH:mm")
    @NotNull(message = "La hora de inicio es obligatoria.")
    LocalTime startTime,

    @Schema(description = "Hora de fin; menor que el inicio indica cruce de medianoche.", example = "17:00")
    @JsonFormat(pattern = "HH:mm")
    @NotNull(message = "La hora de fin es obligatoria.")
    LocalTime endTime
) {}
