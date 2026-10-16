package com.assessment.employees.application.usecase;

import com.assessment.employees.application.dto.EmployeeResult;
import com.assessment.employees.domain.exception.EmployeeNotFoundException;
import com.assessment.employees.domain.model.Document;
import com.assessment.employees.domain.repository.EmployeeRepository;

/**
 * Use case for retrieving an employee by ID.
 */
public class GetEmployee {
    private final EmployeeRepository repository;
    
    public GetEmployee(EmployeeRepository repository) {
        this.repository = repository;
    }
    
    public EmployeeResult execute(String documentType, String documentNumber) {
        Document document = Document.of(documentType, documentNumber);
        return repository.findByDocument(document)
            .map(EmployeeResult::fromDomain)
            .orElseThrow(() -> new EmployeeNotFoundException(document));
    }
}