package com.assessment.employees.domain.repository;

import com.assessment.employees.domain.model.Employee;
import com.assessment.employees.domain.model.EmployeeId;
import com.assessment.employees.domain.model.Document;

import java.util.List;
import java.util.Optional;

/**
 * Port for Employee persistence operations.
 * El empleado se identifica siempre por su clave natural
 * (tipo y número de documento).
 */
public interface EmployeeRepository {
    /**
     * Saves an employee.
     */
    Employee save(Employee employee);

    /**
     * Finds an employee by technical internal ID.
     */
    Optional<Employee> findById(EmployeeId id);

    /**
     * Finds an employee by its natural identity (document type + number).
     */
    Optional<Employee> findByDocument(Document document);

    /**
     * Finds an employee by email.
     */
    Optional<Employee> findByEmail(String email);

    /**
     * Deletes an employee by ID. The deletion cascades to work shifts.
     */
    void deleteById(EmployeeId id);

    /**
     * Finds a stable page of employees.
     */
    List<Employee> findPage(int offset, int limit);

    /**
     * Returns the total number of employees.
     */
    long count();
}
