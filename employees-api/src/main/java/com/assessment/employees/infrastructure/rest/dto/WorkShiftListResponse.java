package com.assessment.employees.infrastructure.rest.dto;

import java.time.Duration;
import java.util.List;

/**
 * Response DTO for work shift list with totals.
 */
public record WorkShiftListResponse(
    List<WorkShiftResponse> shifts,
    Duration totalWorkedHours,
    Duration totalDaytimeHours,
    Duration totalNighttimeHours,
    List<WeeklySummaryResponse> weeklySummaries
) {}