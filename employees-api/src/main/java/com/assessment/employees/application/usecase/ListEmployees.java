package com.assessment.employees.application.usecase;

import com.assessment.employees.application.dto.EmployeePageResult;
import com.assessment.employees.application.dto.EmployeeResult;
import com.assessment.employees.domain.repository.EmployeeRepository;

/**
 * Use case for listing employees with pagination.
 */
public class ListEmployees {
    private final EmployeeRepository repository;
    
    public ListEmployees(EmployeeRepository repository) {
        this.repository = repository;
    }
    
    public EmployeePageResult execute(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("La página o el tamaño solicitado no son válidos.");
        }

        int offset;
        try {
            offset = Math.multiplyExact(page, size);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("El número de página excede el límite permitido.");
        }

        return new EmployeePageResult(
            repository.findPage(offset, size).stream()
                .map(EmployeeResult::fromDomain)
                .toList(),
            page,
            size,
            repository.count()
        );
    }
}