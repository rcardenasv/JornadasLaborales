package com.assessment.employees.infrastructure.rest.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Request DTO for the all-or-nothing bulk creation: one time range applied to
 * the selected weekdays inside the range. Omitting weekdays means every day.
 */
@Schema(
    name = "BulkCreateWorkShiftsRequest",
    description = "Crea una jornada en cada día seleccionado dentro del rango (máximo 92 días).",
    example = "{\"from\":\"2026-10-19\",\"to\":\"2026-10-23\","
        + "\"weekdays\":[\"MONDAY\",\"TUESDAY\",\"WEDNESDAY\",\"THURSDAY\",\"FRIDAY\"],"
        + "\"startTime\":\"08:00\",\"endTime\":\"12:00\"}"
)
public record BulkCreateWorkShiftsRequest(
    @Schema(description = "Fecha inicial inclusiva del rango.", example = "2026-10-19")
    @NotNull(message = "La fecha inicial es obligatoria.")
    LocalDate from,
    @Schema(description = "Fecha final inclusiva del rango.", example = "2026-10-23")
    @NotNull(message = "La fecha final es obligatoria.")
    LocalDate to,
    @Schema(description = "Días de la semana a programar; omitido = todos los días.",
        example = "[\"MONDAY\",\"WEDNESDAY\",\"FRIDAY\"]")
    @Size(min = 1, message = "Debe seleccionar al menos un día de la semana.")
    Set<DayOfWeek> weekdays,
    @Schema(description = "Hora de inicio de la franja.", example = "08:00")
    @JsonFormat(pattern = "HH:mm")
    @NotNull(message = "La hora de inicio es obligatoria.")
    LocalTime startTime,
    @Schema(description = "Hora de fin de la franja; menor que el inicio indica cruce de medianoche.",
        example = "12:00")
    @JsonFormat(pattern = "HH:mm")
    @NotNull(message = "La hora de fin es obligatoria.")
    LocalTime endTime
) {
}
