package com.assessment.employees.infrastructure.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Weekly workload summary for a whole ISO week, with the ordinary limit in
 * effect that week (Ley 2101 de 2021) and its overtime components.
 */
@Schema(name = "WeeklySummary", description = "Carga semanal de una semana ISO completa.")
public record WeeklySummaryResponse(
    @Schema(description = "Semana ISO (yyyy-Www).", example = "2026-W42")
    String week,
    @Schema(description = "Lunes de la semana.", example = "2026-10-12")
    LocalDate startDate,
    @Schema(description = "Domingo de la semana.", example = "2026-10-18")
    LocalDate endDate,
    @Schema(description = "Horas programadas en toda la semana.", example = "46.00")
    BigDecimal scheduledHours,
    @Schema(description = "Límite ordinario vigente para la semana (horas).", example = "42.00")
    BigDecimal ordinaryLimitHours,
    @Schema(description = "Horas extra: exceso sobre el límite ordinario (máximo 12).", example = "4.00")
    BigDecimal overtimeHours,
    @Schema(description = "Horas ordinarias aún disponibles (0.00 si ya hay extra).", example = "34.00")
    BigDecimal remainingOrdinaryHours
) {
}
