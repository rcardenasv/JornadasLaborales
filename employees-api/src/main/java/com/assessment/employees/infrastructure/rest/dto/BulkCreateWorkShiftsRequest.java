package com.assessment.employees.infrastructure.rest.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

@Schema(
    name = "BulkCreateWorkShiftsRequest",
    description = "Crea una jornada en cada día seleccionado dentro del rango (máximo 92 días).",
    example = "{\"from\":\"2026-10-19\",\"to\":\"2026-10-23\","
        + "\"weekdays\":[\"MONDAY\",\"TUESDAY\",\"WEDNESDAY\",\"THURSDAY\",\"FRIDAY\"],"
        + "\"startTime\":\"08:00:00\",\"endTime\":\"12:00:00\"}"
)
public record BulkCreateWorkShiftsRequest(
    @NotNull(message = "La fecha inicial es obligatoria.")
    LocalDate from,
    @NotNull(message = "La fecha final es obligatoria.")
    LocalDate to,
    @Size(min = 1, message = "Debe seleccionar al menos un día de la semana.")
    Set<DayOfWeek> weekdays,
    @NotNull(message = "La hora de inicio es obligatoria.")
    LocalTime startTime,
    @NotNull(message = "La hora de fin es obligatoria.")
    LocalTime endTime
) {
}
