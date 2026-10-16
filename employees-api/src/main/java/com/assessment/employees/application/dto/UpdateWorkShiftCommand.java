package com.assessment.employees.application.dto;

import com.assessment.employees.domain.model.Document;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Command to update an existing work shift.
 */
public record UpdateWorkShiftCommand(
    UUID id,
    Document document,
    LocalDate date,
    LocalTime startTime,
    LocalTime endTime
) {
}
