package com.assessment.employees.application.dto;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import com.assessment.employees.domain.model.DayType;
import com.assessment.employees.domain.model.ShiftType;

/**
 * Result object for work shift operations.
 * No expone el documento del empleado: viaja en la URL de la colección.
 */
public record WorkShiftResult(
    UUID id,
    LocalDate date,
    LocalTime startTime,
    LocalTime endTime,
    Duration workedHours,
    Duration daytimeHours,
    Duration nighttimeHours,
    DayType dayType,
    ShiftType shiftType
) {
    public static WorkShiftResult fromDomain(com.assessment.employees.domain.model.WorkShift workShift) {
        return new WorkShiftResult(
            workShift.getId(),
            workShift.getDate(),
            workShift.getStartTime(),
            workShift.getEndTime(),
            workShift.getWorkedHours(),
            workShift.getDaytimeHours(),
            workShift.getNighttimeHours(),
            workShift.getDayType(),
            workShift.getShiftType()
        );
    }
}
