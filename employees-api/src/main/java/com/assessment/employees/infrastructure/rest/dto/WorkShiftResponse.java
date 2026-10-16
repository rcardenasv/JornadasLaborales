package com.assessment.employees.infrastructure.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import com.assessment.employees.domain.model.DayType;
import com.assessment.employees.domain.model.ShiftType;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Response DTO for a single work shift. Hours are decimal with two places;
 * start/end times use the HH:mm contract. No document fields: they live in the URL.
 */
@Schema(name = "WorkShift", description = "Jornada laboral con su clasificación calculada.")
public record WorkShiftResponse(
    @Schema(description = "Identificador único de la jornada.", example = "650e8400-e29b-41d4-a716-446655440004")
    UUID id,
    @Schema(description = "Fecha de inicio de la jornada (día de inicio).", example = "2026-10-15")
    LocalDate date,
    @Schema(description = "Hora de inicio.", example = "10:00")
    @JsonFormat(pattern = "HH:mm")
    LocalTime startTime,
    @Schema(description = "Hora de fin; menor que el inicio indica cruce de medianoche.", example = "18:00")
    @JsonFormat(pattern = "HH:mm")
    LocalTime endTime,
    @Schema(description = "Tipo de día calculado: ORDINARIO, DOMINICAL o FESTIVO.", example = "ORDINARIO")
    DayType dayType,
    @Schema(description = "Tipo de turno calculado: DIURNA, NOCTURNA o MIXTA.", example = "DIURNA")
    ShiftType shiftType,
    @Schema(description = "Horas trabajadas totales (decimales).", example = "8.00")
    BigDecimal workedHours,
    @Schema(description = "Horas dentro de la ventana diurna vigente (decimales).", example = "8.00")
    BigDecimal daytimeHours,
    @Schema(description = "Horas dentro de la ventana nocturna vigente (decimales).", example = "0.00")
    BigDecimal nighttimeHours
) {}
