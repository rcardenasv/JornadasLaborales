package com.assessment.employees.application.usecase;

import com.assessment.employees.domain.exception.EmployeeNotFoundException;
import com.assessment.employees.domain.model.Document;
import com.assessment.employees.domain.model.Employee;
import com.assessment.employees.domain.repository.EmployeeRepository;

/**
 * Use case for deleting an employee.
 */
public class DeleteEmployee {
    private final EmployeeRepository repository;
    
    public DeleteEmployee(EmployeeRepository repository) {
        this.repository = repository;
    }
    
    public void execute(String documentType, String documentNumber) {
        Document document = Document.of(documentType, documentNumber);
        Employee employee = repository.findByDocument(document)
            .orElseThrow(() -> new EmployeeNotFoundException(document));
        repository.deleteById(employee.getId());
    }
}