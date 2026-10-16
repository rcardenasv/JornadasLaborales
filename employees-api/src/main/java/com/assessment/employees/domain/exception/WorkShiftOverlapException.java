package com.assessment.employees.domain.exception;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Exception thrown when work shifts overlap.
 */
public class WorkShiftOverlapException extends DomainException {
    public WorkShiftOverlapException(LocalDate date, LocalTime startTime, LocalTime endTime) {
        super("Work shift overlaps with existing shift on " + date + 
              " between " + startTime + " and " + endTime);
    }
}