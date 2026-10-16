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
        Document document = command.document();
        Employee existing = repository.findByDocument(document)
            .orElseThrow(() -> new EmployeeNotFoundException(document));
        validateAtLeastOneField(command);
        if (command.hireDate() != null && !existing.getHireDate().equals(command.hireDate())) {
            throw new InvalidEmployeeDataException("La fecha de contratación no se puede modificar.");
        }

        // Check for duplicate email only when a new email is provided
        if (command.email() != null) {
            repository.findByEmail(command.email()).ifPresent(emp -> {
                if (!emp.getDocument().equals(document)) {
                    throw new DuplicateEmployeeException("correo");
                }
            });
        }

        // Null fields keep the current value; provided fields are validated by the domain
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

    private void validateAtLeastOneField(UpdateEmployeeCommand command) {
        boolean anyEditableField = command.firstName() != null || command.lastName() != null
            || command.email() != null || command.position() != null;
        if (!anyEditableField) {
            throw new InvalidEmployeeDataException("Debe enviar al menos un campo a actualizar.");
        }
    }
}