package com.assessment.employees.application.usecase;

import com.assessment.employees.application.dto.CreateEmployeeCommand;
import com.assessment.employees.application.dto.EmployeeResult;
import com.assessment.employees.domain.exception.DuplicateEmployeeException;
import com.assessment.employees.domain.model.Document;
import com.assessment.employees.domain.model.Employee;
import com.assessment.employees.domain.repository.EmployeeRepository;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Use case for creating an employee. El `Clock` permite validar de
 * forma determinista que `hireDate` no sea futura.
 */
public class CreateEmployee {
    private final EmployeeRepository repository;
    private final Clock clock;

    public CreateEmployee(EmployeeRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public EmployeeResult execute(CreateEmployeeCommand command) {
        Document document = command.document();
        // Check for duplicate email
        if (repository.findByEmail(command.email()).isPresent()) {
            throw new DuplicateEmployeeException("correo");
        }

        // Check for duplicate document number
        if (repository.findByDocument(document).isPresent()) {
            throw new DuplicateEmployeeException("documento");
        }

        // Create employee
        Employee employee = Employee.create(
            document,
            command.firstName(),
            command.lastName(),
            command.email(),
            command.position(),
            command.hireDate(),
            LocalDate.ofInstant(clock.instant(), clock.getZone())
        );

        // Save to repository
        Employee saved = repository.save(employee);

        return EmployeeResult.fromDomain(saved);
    }
}
