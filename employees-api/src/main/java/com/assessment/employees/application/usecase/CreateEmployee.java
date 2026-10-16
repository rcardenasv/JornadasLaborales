package com.assessment.employees.application.usecase;

import com.assessment.employees.application.dto.CreateEmployeeCommand;
import com.assessment.employees.application.dto.EmployeeResult;
import com.assessment.employees.domain.exception.DuplicateEmployeeException;
import com.assessment.employees.domain.model.Employee;
import com.assessment.employees.domain.repository.EmployeeRepository;

/**
 * Use case for creating an employee.
 */
public class CreateEmployee {
    private final EmployeeRepository repository;
    
    public CreateEmployee(EmployeeRepository repository) {
        this.repository = repository;
    }
    
    public EmployeeResult execute(CreateEmployeeCommand command) {
        // Check for duplicate email
        if (repository.findByEmail(command.email()).isPresent()) {
            throw new DuplicateEmployeeException("email", command.email());
        }
        
        // Check for duplicate document number
        if (repository.findByDocumentNumber(command.documentNumber()).isPresent()) {
            throw new DuplicateEmployeeException("document number", command.documentNumber());
        }
        
        // Create employee
        Employee employee = Employee.create(
            command.documentNumber(),
            command.firstName(),
            command.lastName(),
            command.email(),
            command.position(),
            command.hireDate()
        );
        
        // Save to repository
        Employee saved = repository.save(employee);
        
        return EmployeeResult.fromDomain(saved);
    }
}