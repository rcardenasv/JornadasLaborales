package com.assessment.employees.application.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Command to create a new work shift.
 */
public record CreateWorkShiftCommand(
    String documentType,
    String documentNumber,
    LocalDate date,
    LocalTime startTime,
    LocalTime endTime
) {
}