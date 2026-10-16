package com.assessment.employees.application.usecase;

import com.assessment.employees.domain.exception.EmployeeNotFoundException;
import com.assessment.employees.domain.model.EmployeeId;
import com.assessment.employees.domain.repository.EmployeeRepository;

/**
 * Use case for deleting an employee.
 */
public class DeleteEmployee {
    private final EmployeeRepository repository;
    
    public DeleteEmployee(EmployeeRepository repository) {
        this.repository = repository;
    }
    
    public void execute(String id) {
        EmployeeId employeeId = new EmployeeId(java.util.UUID.fromString(id));
        
        // Check if employee exists
        if (!repository.existsById(employeeId)) {
            throw new EmployeeNotFoundException(employeeId);
        }
        
        // Delete associated work shifts first (cascada will handle this, but we clean up explicitly)
        repository.deleteWorkShiftsByEmployeeId(employeeId);
        
        // Delete employee
        repository.deleteById(employeeId);
    }
}