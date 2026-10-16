package com.assessment.employees.domain.repository;

import com.assessment.employees.domain.model.Employee;
import com.assessment.employees.domain.model.EmployeeId;
import com.assessment.employees.domain.model.Document;

import java.util.List;
import java.util.Optional;

/**
 * Port for Employee persistence operations.
 * Defines the interface for data access in the domain layer.
 */
public interface EmployeeRepository {
    /**
     * Saves an employee.
     */
    Employee save(Employee employee);
    
    /**
     * Finds an employee by ID.
     */
    Optional<Employee> findById(EmployeeId id);
    
    /**
     * Finds an employee by document number.
     */
    Optional<Employee> findByDocumentNumber(String documentNumber);

    default Optional<Employee> findByDocument(Document document) {
        return findByDocumentNumber(document.number())
            .filter(employee -> employee.getDocument().equals(document));
    }
    
    /**
     * Finds an employee by email.
     */
    Optional<Employee> findByEmail(String email);
    
    /**
     * Deletes an employee by ID.
     */
    void deleteById(EmployeeId id);

    default void deleteByDocument(Document document) {
        findByDocument(document).ifPresent(employee -> deleteById(employee.getId()));
    }

    /**
     * Checks if an employee exists by ID.
     */
    boolean existsById(EmployeeId id);

    default boolean existsByDocument(Document document) {
        return findByDocument(document).isPresent();
    }

    /**
     * Finds a stable page of employees.
     */
    List<Employee> findPage(int offset, int limit);

    /**
     * Returns the total number of employees.
     */
    long count();
}