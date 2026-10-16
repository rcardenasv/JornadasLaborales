package com.assessment.employees.infrastructure.rest.dto;

import java.time.Duration;
import java.time.LocalDate;

public record WeeklySummaryResponse(
    String week,
    LocalDate startDate,
    LocalDate endDate,
    Duration scheduledHours,
    Duration maxWeeklyHours,
    Duration remainingHours
) {
}
