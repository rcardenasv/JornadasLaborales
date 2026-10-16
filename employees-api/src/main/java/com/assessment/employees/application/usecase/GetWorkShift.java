package com.assessment.employees.application.usecase;

import com.assessment.employees.application.dto.WorkShiftResult;
import com.assessment.employees.domain.exception.WorkShiftNotFoundException;
import com.assessment.employees.domain.repository.WorkShiftRepository;

import java.util.UUID;

/**
 * Use case for retrieving a work shift by ID.
 */
public class GetWorkShift {
    private final WorkShiftRepository repository;
    
    public GetWorkShift(WorkShiftRepository repository) {
        this.repository = repository;
    }
    
    public WorkShiftResult execute(String shiftId, String employeeId) {
        java.util.UUID id = java.util.UUID.fromString(shiftId);
        
        return repository.findById(id)
            .map(result -> {
                // Verify the work shift belongs to the specified employee
                if (!result.getEmployeeId().value().equals(UUID.fromString(employeeId))) {
                    throw new WorkShiftNotFoundException(id);
                }
                return WorkShiftResult.fromDomain(result);
            })
            .orElseThrow(() -> new WorkShiftNotFoundException(id));
    }
}