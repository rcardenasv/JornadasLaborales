package com.assessment.employees.application.usecase;

import com.assessment.employees.domain.exception.EmployeeNotFoundException;
import com.assessment.employees.domain.exception.WorkShiftNotFoundException;
import com.assessment.employees.domain.model.EmployeeId;
import com.assessment.employees.domain.repository.EmployeeRepository;
import com.assessment.employees.domain.repository.WorkShiftRepository;

import java.util.UUID;

/**
 * Use case for deleting a work shift.
 */
public class DeleteWorkShift {
    private final WorkShiftRepository workShiftRepository;
    private final EmployeeRepository employeeRepository;
    
    public DeleteWorkShift(WorkShiftRepository workShiftRepository,
                           EmployeeRepository employeeRepository) {
        this.workShiftRepository = workShiftRepository;
        this.employeeRepository = employeeRepository;
    }
    
    public void execute(String shiftId, String employeeId) {
        java.util.UUID id = java.util.UUID.fromString(shiftId);
        EmployeeId empId = new EmployeeId(UUID.fromString(employeeId));
        
        // Check if work shift exists and belongs to employee
        var existing = workShiftRepository.findById(id)
            .orElseThrow(() -> new WorkShiftNotFoundException(id));
        
        if (!existing.getEmployeeId().value().equals(empId.value())) {
            throw new WorkShiftNotFoundException(id);
        }
        
        // Check if employee exists
        if (!employeeRepository.existsById(empId)) {
            throw new EmployeeNotFoundException(empId);
        }
        
        // Delete work shift
        workShiftRepository.deleteById(id);
    }
}