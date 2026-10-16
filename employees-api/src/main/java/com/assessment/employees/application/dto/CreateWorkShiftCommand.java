package com.assessment.employees.application.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Command to create a new work shift.
 */
public record CreateWorkShiftCommand(
    String employeeId,
    LocalDate date,
    LocalTime startTime,
    LocalTime endTime
) {
}