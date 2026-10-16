package com.assessment.employees.domain.model;

import java.time.LocalTime;

/**
 * Value object representing a time of day.
 */
public record Time(LocalTime value) {
    public Time {
        if (value == null) {
            throw new IllegalArgumentException("Time cannot be null");
        }
    }
    
    public static Time of(LocalTime value) {
        return new Time(value);
    }
    
    public static Time of(int hour, int minute) {
        return new Time(LocalTime.of(hour, minute));
    }
    
    public boolean isBefore(Time other) {
        return value.isBefore(other.value);
    }
    
    public boolean isAfter(Time other) {
        return value.isAfter(other.value);
    }
    
    public long differenceInMinutes(Time other) {
        return value.until(other.value, java.time.temporal.ChronoUnit.MINUTES);
    }
}