package com.assessment.employees.application.usecase;

import com.assessment.employees.application.dto.EmployeeResult;
import com.assessment.employees.domain.exception.EmployeeNotFoundException;
import com.assessment.employees.domain.model.EmployeeId;
import com.assessment.employees.domain.repository.EmployeeRepository;

/**
 * Use case for retrieving an employee by ID.
 */
public class GetEmployee {
    private final EmployeeRepository repository;
    
    public GetEmployee(EmployeeRepository repository) {
        this.repository = repository;
    }
    
    public EmployeeResult execute(String id) {
        EmployeeId employeeId = new EmployeeId(java.util.UUID.fromString(id));
        
        return repository.findById(employeeId)
            .map(EmployeeResult::fromDomain)
            .orElseThrow(() -> new EmployeeNotFoundException(employeeId));
    }
}