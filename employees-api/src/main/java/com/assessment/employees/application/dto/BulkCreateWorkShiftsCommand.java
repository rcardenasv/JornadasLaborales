package com.assessment.employees.application.dto;

import com.assessment.employees.domain.model.Document;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

public record BulkCreateWorkShiftsCommand(
    Document document,
    LocalDate from,
    LocalDate to,
    Set<DayOfWeek> weekdays,
    LocalTime startTime,
    LocalTime endTime
) {
}
