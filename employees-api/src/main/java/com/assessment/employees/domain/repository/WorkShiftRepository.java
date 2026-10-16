package com.assessment.employees.domain.repository;

import com.assessment.employees.domain.model.Document;
import com.assessment.employees.domain.model.WorkShift;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port for WorkShift persistence operations.
 * Defines the interface for data access in the domain layer.
 */
public interface WorkShiftRepository {
    /**
     * Saves a work shift.
     */
    WorkShift save(WorkShift workShift);

    /**
     * Saves all shifts as one atomic operation.
     */
    List<WorkShift> saveAll(List<WorkShift> workShifts);
    
    /**
     * Finds a work shift by ID.
     */
    Optional<WorkShift> findById(UUID id);
    
    /**
     * Finds all work shifts for an employee.
     */
    List<WorkShift> findByEmployeeDocument(Document employeeDocument);
    
    /**
     * Finds work shifts for an employee within a date range.
     */
    List<WorkShift> findByEmployeeDocumentAndDateRange(Document employeeDocument,
                                                       LocalDate from, LocalDate to);
    
    /**
     * Deletes a work shift by ID.
     */
    void deleteById(UUID id);
    
}