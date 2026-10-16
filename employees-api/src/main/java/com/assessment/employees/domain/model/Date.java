package com.assessment.employees.domain.model;

import java.time.LocalDate;

/**
 * Value object representing a work shift date.
 */
public record Date(LocalDate value) {
    public Date {
        if (value == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
    }
    
    public static Date of(LocalDate value) {
        return new Date(value);
    }
    
    public boolean isBefore(Date other) {
        return value.isBefore(other.value);
    }
    
    public boolean isAfter(Date other) {
        return value.isAfter(other.value);
    }
    
    public boolean isBeforeOrEqual(Date other) {
        return value.compareTo(other.value) <= 0;
    }
    
    public boolean isAfterOrEqual(Date other) {
        return value.compareTo(other.value) >= 0;
    }
}