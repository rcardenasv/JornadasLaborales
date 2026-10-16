package com.assessment.employees.domain.exception;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Exception thrown when work shifts overlap.
 */
public class WorkShiftOverlapException extends DomainException {
    public WorkShiftOverlapException(LocalDate date, LocalTime startTime, LocalTime endTime) {
        super("La jornada del " + date + " (de " + startTime + " a " + endTime
            + ") se solapa con otra jornada del empleado.");
    }
}