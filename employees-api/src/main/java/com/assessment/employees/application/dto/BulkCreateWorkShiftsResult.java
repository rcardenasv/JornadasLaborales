package com.assessment.employees.application.dto;

import java.time.Duration;
import java.util.List;

public record BulkCreateWorkShiftsResult(
    List<WorkShiftResult> createdShifts,
    Duration scheduledHours
) {
}
