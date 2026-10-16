package com.assessment.employees.domain.model;

import java.util.UUID;

/**
 * Value object representing an Employee identifier.
 */
public record EmployeeId(UUID value) {
    public EmployeeId {
        if (value == null) {
            throw new IllegalArgumentException("EmployeeId cannot be null");
        }
    }
    
    public static EmployeeId random() {
        return new EmployeeId(UUID.randomUUID());
    }
    
    public static EmployeeId of(UUID value) {
        return new EmployeeId(value);
    }
    
    @Override
    public String toString() {
        return value.toString();
    }
}