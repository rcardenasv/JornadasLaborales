package com.assessment.employees.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * JPA entity for WorkShift.
 */
@Entity
@Table(name = "work_shifts", indexes = {
    @Index(name = "idx_work_shifts_employee_date",
        columnList = "employee_document_type, employee_document_number, date")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uq_work_shifts_employee_date_time",
        columnNames = {"employee_document_type", "employee_document_number", "date", "start_time"})
})
public class WorkShiftEntity {
    
    @Id
    @Column(name = "id")
    private UUID id;
    
    @Column(name = "employee_document_type", nullable = false, length = 2)
    private String employeeDocumentType;

    @Column(name = "employee_document_number", nullable = false, length = 20)
    private String employeeDocumentNumber;
    
    @Column(name = "date", nullable = false)
    private LocalDate date;
    
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
    
    // Default constructor
    public WorkShiftEntity() {}
    
    public WorkShiftEntity(UUID id, String employeeDocumentType, String employeeDocumentNumber,
                           LocalDate date,
                          LocalTime startTime, LocalTime endTime) {
        this.id = id;
        this.employeeDocumentType = employeeDocumentType;
        this.employeeDocumentNumber = employeeDocumentNumber;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getEmployeeDocumentType() { return employeeDocumentType; }
    public void setEmployeeDocumentType(String employeeDocumentType) {
        this.employeeDocumentType = employeeDocumentType;
    }

    public String getEmployeeDocumentNumber() { return employeeDocumentNumber; }
    public void setEmployeeDocumentNumber(String employeeDocumentNumber) {
        this.employeeDocumentNumber = employeeDocumentNumber;
    }
    
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
}