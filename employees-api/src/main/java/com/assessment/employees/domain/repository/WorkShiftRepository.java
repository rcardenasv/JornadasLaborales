package com.assessment.employees.domain.repository;

import com.assessment.employees.domain.model.EmployeeId;
import com.assessment.employees.domain.model.WorkShift;

import java.time.LocalDate;
import java.time.LocalTime;
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
     * Finds a work shift by ID.
     */
    Optional<WorkShift> findById(UUID id);
    
    /**
     * Finds all work shifts for an employee.
     */
    List<WorkShift> findByEmployeeId(EmployeeId employeeId);
    
    /**
     * Finds work shifts for an employee within a date range.
     */
    List<WorkShift> findByEmployeeIdAndDateRange(EmployeeId employeeId,
                                                  LocalDate from, LocalDate to);
    
    /**
     * Finds overlapping work shifts for an employee on a specific date.
     * Used to prevent scheduling conflicts.
     */
    List<WorkShift> findOverlapping(EmployeeId employeeId, LocalDate date, 
                                    LocalTime startTime, LocalTime endTime);
    
    /**
     * Deletes a work shift by ID.
     */
    void deleteById(UUID id);
    
    /**
     * Deletes all work shifts for an employee.
     */
    void deleteByEmployeeId(EmployeeId employeeId);
}