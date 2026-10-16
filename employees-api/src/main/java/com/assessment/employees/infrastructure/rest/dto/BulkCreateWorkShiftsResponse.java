package com.assessment.employees.infrastructure.rest.dto;

import java.math.BigDecimal;
import java.util.List;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "BulkCreateWorkShifts", description = "Resultado de la carga agrupada todo-o-nada.")
public record BulkCreateWorkShiftsResponse(
    @Schema(description = "Jornadas creadas.")
    List<WorkShiftResponse> createdShifts,
    @Schema(description = "Cantidad de jornadas creadas.", example = "3")
    int count,
    @Schema(description = "Horas totales programadas por la carga.", example = "24.00")
    BigDecimal scheduledHours
) {
}
