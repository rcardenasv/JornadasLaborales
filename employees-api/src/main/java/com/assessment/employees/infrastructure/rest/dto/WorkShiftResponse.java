package com.assessment.employees.infrastructure.rest.dto;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import com.assessment.employees.domain.model.DayType;
import com.assessment.employees.domain.model.ShiftType;

/**
 * Response DTO for work shift data.
 */
public record WorkShiftResponse(
    UUID id,
    String documentType,
    String documentNumber,
    LocalDate date,
    LocalTime startTime,
    LocalTime endTime,
    Duration workedHours,
    DayType dayType,
    ShiftType shiftType,
    Duration daytimeHours,
    Duration nighttimeHours
) {}