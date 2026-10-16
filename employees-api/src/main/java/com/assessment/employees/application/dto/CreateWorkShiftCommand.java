package com.assessment.employees.application.dto;

import com.assessment.employees.domain.model.Document;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Command to create a new work shift.
 */
public record CreateWorkShiftCommand(
    Document document,
    LocalDate date,
    LocalTime startTime,
    LocalTime endTime
) {
}
