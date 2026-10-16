package com.assessment.employees.application.usecase;

import com.assessment.employees.domain.exception.EmployeeNotFoundException;
import com.assessment.employees.domain.exception.WorkShiftNotFoundException;
import com.assessment.employees.domain.model.Document;
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
    
    public void execute(UUID id, Document document) {
        // Check if work shift exists and belongs to employee
        var existing = workShiftRepository.findById(id)
            .orElseThrow(() -> new WorkShiftNotFoundException(id));
        
        if (!existing.getEmployeeDocument().equals(document)) {
            throw new WorkShiftNotFoundException(id);
        }
        
        // Check if employee exists
        if (employeeRepository.findByDocument(document).isEmpty()) {
            throw new EmployeeNotFoundException(document);
        }
        
        // Delete work shift
        workShiftRepository.deleteById(id);
    }
}