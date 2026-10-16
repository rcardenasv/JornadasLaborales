package com.assessment.employees.application.usecase;

import com.assessment.employees.application.dto.EmployeeResult;
import com.assessment.employees.application.dto.UpdateEmployeeCommand;
import com.assessment.employees.domain.exception.DuplicateEmployeeException;
import com.assessment.employees.domain.exception.EmployeeNotFoundException;
import com.assessment.employees.domain.model.Employee;
import com.assessment.employees.domain.model.EmployeeId;
import com.assessment.employees.domain.repository.EmployeeRepository;

/**
 * Use case for updating an employee.
 */
public class UpdateEmployee {
    private final EmployeeRepository repository;
    
    public UpdateEmployee(EmployeeRepository repository) {
        this.repository = repository;
    }
    
    public EmployeeResult execute(UpdateEmployeeCommand command) {
        EmployeeId employeeId = new EmployeeId(java.util.UUID.fromString(command.id()));
        
        // Check if employee exists
        Employee existing = repository.findById(employeeId)
            .orElseThrow(() -> new EmployeeNotFoundException(employeeId));
        
        // Check for duplicate email (excluding current employee)
        repository.findByEmail(command.email()).ifPresent(emp -> {
            if (!emp.getId().equals(employeeId)) {
                throw new DuplicateEmployeeException("email", command.email());
            }
        });
        
        // Check for duplicate document number (excluding current employee)
        repository.findByDocumentNumber(command.documentNumber()).ifPresent(emp -> {
            if (!emp.getId().equals(employeeId)) {
                throw new DuplicateEmployeeException("document number", command.documentNumber());
            }
        });
        
        // Update employee
        existing.update(
            command.documentNumber(),
            command.firstName(),
            command.lastName(),
            command.email(),
            command.position(),
            command.hireDate()
        );
        
        // Save to repository
        Employee updated = repository.save(existing);
        
        return EmployeeResult.fromDomain(updated);
    }
}