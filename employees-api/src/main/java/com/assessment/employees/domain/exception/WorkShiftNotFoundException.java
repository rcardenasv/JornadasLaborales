package com.assessment.employees.domain.exception;

import java.util.UUID;

/**
 * Exception thrown when a work shift is not found.
 */
public class WorkShiftNotFoundException extends DomainException {
    public WorkShiftNotFoundException(UUID id) {
        super("Work shift with id " + id + " not found");
    }
}