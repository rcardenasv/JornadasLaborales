package com.assessment.employees.application.usecase;

import com.assessment.employees.application.dto.EmployeeResult;
import com.assessment.employees.application.dto.UpdateEmployeeCommand;
import com.assessment.employees.domain.exception.DuplicateEmployeeException;
import com.assessment.employees.domain.exception.EmployeeNotFoundException;
import com.assessment.employees.domain.exception.InvalidEmployeeDataException;
import com.assessment.employees.domain.model.Document;
import com.assessment.employees.domain.model.Employee;
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
        Document document = Document.of(command.documentType(), command.documentNumber());
        Employee existing = repository.findByDocument(document)
            .orElseThrow(() -> new EmployeeNotFoundException(document));
        if (!existing.getHireDate().equals(command.hireDate())) {
            throw new InvalidEmployeeDataException("La fecha de contratación no se puede modificar.");
        }
        
        // Check for duplicate email (excluding current employee)
        repository.findByEmail(command.email()).ifPresent(emp -> {
            if (!emp.getDocument().equals(document)) {
                throw new DuplicateEmployeeException("email", command.email());
            }
        });
        
        // Check for duplicate document number (excluding current employee)
        existing.update(
            command.firstName(),
            command.lastName(),
            command.email(),
            command.position()
        );
        
        // Save to repository
        Employee updated = repository.save(existing);
        
        return EmployeeResult.fromDomain(updated);
    }

}