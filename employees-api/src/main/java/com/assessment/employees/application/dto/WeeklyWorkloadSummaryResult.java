package com.assessment.employees.application.dto;

import java.time.Duration;
import java.time.LocalDate;

public record WeeklyWorkloadSummaryResult(LocalDate weekStart, Duration scheduledHours) {
}
