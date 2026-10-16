package com.assessment.employees.application.dto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

public record BulkCreateWorkShiftsCommand(
    String documentType,
    String documentNumber,
    LocalDate from,
    LocalDate to,
    Set<DayOfWeek> weekdays,
    LocalTime startTime,
    LocalTime endTime
) {
}
