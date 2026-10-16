package com.assessment.employees.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * JPA entity for WorkShift.
 */
@Entity
@Table(name = "work_shifts")
public class WorkShiftEntity {
    
    @Id
    @Column(name = "id")
    private UUID id;
    
    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;
    
    @Column(name = "date", nullable = false)
    private LocalDate date;
    
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
    
    // Default constructor
    public WorkShiftEntity() {}
    
    public WorkShiftEntity(UUID id, UUID employeeId, LocalDate date,
                          LocalTime startTime, LocalTime endTime) {
        this.id = id;
        this.employeeId = employeeId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public UUID getEmployeeId() { return employeeId; }
    public void setEmployeeId(UUID employeeId) { this.employeeId = employeeId; }
    
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
}