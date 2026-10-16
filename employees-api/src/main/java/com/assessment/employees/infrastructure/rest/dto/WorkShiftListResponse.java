package com.assessment.employees.infrastructure.rest.dto;

import java.math.BigDecimal;
import java.util.List;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Response DTO for the work shift list: exact totals of the listed range and
 * weekly summaries of the whole ISO weeks touched by it.
 */
@Schema(name = "WorkShiftList", description = "Listado de jornadas con resumen semanal.")
public record WorkShiftListResponse(
    @Schema(description = "Jornadas ordenadas por fecha y hora de inicio.")
    List<WorkShiftResponse> shifts,
    @Schema(description = "Suma de horas trabajadas de las jornadas listadas.", example = "8.00")
    BigDecimal totalWorkedHours,
    @Schema(description = "Resumen de las semanas ISO completas tocadas por el rango.")
    List<WeeklySummaryResponse> weeklySummaries
) {}
