package com.assessment.employees.application.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Command to update an existing work shift.
 */
public record UpdateWorkShiftCommand(
    UUID id,
    String employeeId,
    LocalDate date,
    LocalTime startTime,
    LocalTime endTime
) {
}