package com.assessment.employees.application.usecase;

import com.assessment.employees.application.dto.EmployeePageResult;
import com.assessment.employees.application.dto.EmployeeResult;
import com.assessment.employees.domain.exception.InvalidPaginationException;
import com.assessment.employees.domain.repository.EmployeeRepository;

/**
 * Use case for listing employees with pagination.
 */
public class ListEmployees {
    private static final int MIN_PAGE_SIZE = 1;
    private static final int MAX_PAGE_SIZE = 100;

    private final EmployeeRepository repository;
    
    public ListEmployees(EmployeeRepository repository) {
        this.repository = repository;
    }
    
    public EmployeePageResult execute(int page, int size) {
        if (page < 0 || size < MIN_PAGE_SIZE || size > MAX_PAGE_SIZE) {
            throw new InvalidPaginationException("La página o el tamaño solicitado no son válidos.");
        }

        int offset;
        try {
            offset = Math.multiplyExact(page, size);
        } catch (ArithmeticException exception) {
            throw new InvalidPaginationException("El número de página excede el límite permitido.");
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