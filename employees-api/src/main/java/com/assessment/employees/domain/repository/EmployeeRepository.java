package com.assessment.employees.domain.repository;

import com.assessment.employees.domain.model.Employee;
import com.assessment.employees.domain.model.EmployeeId;

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
    
    /**
     * Finds an employee by email.
     */
    Optional<Employee> findByEmail(String email);
    
    /**
     * Deletes an employee by ID.
     */
    void deleteById(EmployeeId id);
    
    /**
     * Checks if an employee exists by ID.
     */
    boolean existsById(EmployeeId id);
    
    /**
     * Deletes all work shifts for an employee.
     */
    void deleteWorkShiftsByEmployeeId(EmployeeId employeeId);
    
    /**
     * Finds a stable page of employees.
     */
    List<Employee> findPage(int offset, int limit);

    /**
     * Returns the total number of employees.
     */
    long count();
}