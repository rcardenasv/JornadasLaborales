package com.assessment.employees.domain.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class EmployeeTest {
    @Test
    void shouldCreateEmployee() {
        Employee emp = Employee.create("DOC123", "John", "Doe", "john@test.com", "Dev", LocalDate.now());
        assertNotNull(emp.getId());
        assertEquals("DOC123", emp.getDocumentNumber().value());
    }
}