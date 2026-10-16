package com.assessment.employees.infrastructure.rest.dto;

import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Request DTO for creating a work shift. Times use the HH:mm contract;
 * endTime earlier than startTime means the shift crosses midnight.
 */
@Schema(
    name = "CreateWorkShiftRequest",
    description = "Fecha y horario de la jornada laboral.",
    example = "{\"date\":\"2026-10-15\",\"startTime\":\"10:00\",\"endTime\":\"18:00\"}"
)
public record CreateWorkShiftRequest(
    @Schema(description = "Fecha de inicio de la jornada.", example = "2026-10-15")
    @NotNull(message = "La fecha de la jornada es obligatoria.")
    LocalDate date,

    @Schema(description = "Hora de inicio.", example = "10:00")
    @JsonFormat(pattern = "HH:mm")
    @NotNull(message = "La hora de inicio es obligatoria.")
    LocalTime startTime,

    @Schema(description = "Hora de fin; menor que el inicio indica cruce de medianoche.", example = "18:00")
    @JsonFormat(pattern = "HH:mm")
    @NotNull(message = "La hora de fin es obligatoria.")
    LocalTime endTime
) {}
