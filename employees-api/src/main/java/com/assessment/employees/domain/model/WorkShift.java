package com.assessment.employees.domain.model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * WorkShift aggregate representing a work shift for an employee.
 */
public class WorkShift {
    private UUID id;
    private EmployeeId employeeId;
    private Date date;
    private Time startTime;
    private Time endTime;
    private Duration workedHours;

    public WorkShift(UUID id, EmployeeId employeeId, LocalDate date,
                     LocalTime startTime, LocalTime endTime) {
        this.id = id;
        this.employeeId = employeeId;
        this.date = new Date(date);
        this.startTime = new Time(startTime);
        this.endTime = new Time(endTime);
        this.workedHours = calculateWorkedHours();
        validate();
    }

    private void validate() {
        if (employeeId == null) {
            throw new IllegalArgumentException("EmployeeId cannot be null");
        }
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        if (startTime == null) {
            throw new IllegalArgumentException("Start time cannot be null");
        }
        if (endTime == null) {
            throw new IllegalArgumentException("End time cannot be null");
        }
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
    }

    private Duration calculateWorkedHours() {
        return Duration.between(startTime.value(), endTime.value());
    }

    public static WorkShift create(EmployeeId employeeId, LocalDate date,
                                   LocalTime startTime, LocalTime endTime) {
        return new WorkShift(UUID.randomUUID(), employeeId, date, startTime, endTime);
    }

    public void update(LocalDate date, LocalTime startTime, LocalTime endTime) {
        WorkShift updated = new WorkShift(id, employeeId, date, startTime, endTime);
        this.date = updated.date;
        this.startTime = updated.startTime;
        this.endTime = updated.endTime;
        this.workedHours = updated.workedHours;
    }

    // Getters
    public UUID getId() { return id; }
    public EmployeeId getEmployeeId() { return employeeId; }
    public LocalDate getDate() { return date.value(); }
    public LocalTime getStartTime() { return startTime.value(); }
    public LocalTime getEndTime() { return endTime.value(); }
    public Duration getWorkedHours() { return workedHours; }
}