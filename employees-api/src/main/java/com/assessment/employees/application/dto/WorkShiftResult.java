package com.assessment.employees.application.dto;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Result object for work shift operations.
 */
public record WorkShiftResult(
    UUID id,
    String employeeId,
    LocalDate date,
    LocalTime startTime,
    LocalTime endTime,
    Duration workedHours
) {
    public static WorkShiftResult fromDomain(com.assessment.employees.domain.model.WorkShift workShift) {
        return new WorkShiftResult(
            workShift.getId(),
            workShift.getEmployeeId().value().toString(),
            workShift.getDate(),
            workShift.getStartTime(),
            workShift.getEndTime(),
            workShift.getWorkedHours()
        );
    }
}