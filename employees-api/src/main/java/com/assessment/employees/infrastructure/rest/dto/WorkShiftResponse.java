package com.assessment.employees.infrastructure.rest.dto;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Response DTO for work shift data.
 */
public record WorkShiftResponse(
    UUID id,
    String employeeId,
    LocalDate date,
    LocalTime startTime,
    LocalTime endTime,
    Duration workedHours
) {}