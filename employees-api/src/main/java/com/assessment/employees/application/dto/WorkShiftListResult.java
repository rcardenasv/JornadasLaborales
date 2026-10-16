package com.assessment.employees.application.dto;

import java.util.List;

public record WorkShiftListResult(
    List<WorkShiftResult> shifts,
    List<WeeklyWorkloadSummaryResult> weeklySummaries
) {
}
