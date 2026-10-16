package com.assessment.employees.infrastructure.rest.dto;

import java.time.Duration;
import java.util.List;

public record BulkCreateWorkShiftsResponse(
    List<WorkShiftResponse> createdShifts,
    int count,
    Duration scheduledHours
) {
}
