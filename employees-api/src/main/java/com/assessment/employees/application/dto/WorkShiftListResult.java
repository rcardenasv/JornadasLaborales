package com.assessment.employees.application.dto;

import java.time.Duration;
import java.util.List;

public record WorkShiftListResult(
    List<WorkShiftResult> shifts,
    List<WeeklyWorkloadSummaryResult> weeklySummaries
) {
    /** Suma exacta de las horas trabajadas de las jornadas listadas (rango filtrado). */
    public Duration totalWorkedHours() {
        return shifts.stream()
            .map(WorkShiftResult::workedHours)
            .reduce(Duration.ZERO, Duration::plus);
    }
}
