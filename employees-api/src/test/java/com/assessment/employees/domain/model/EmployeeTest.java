package com.assessment.employees.domain.model;

import com.assessment.employees.domain.exception.InvalidEmployeeDataException;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class EmployeeTest {
    @Test
    void shouldCreateEmployee() {
        Employee emp = Employee.create("DOC123", "John", "Doe", "john@test.com", "Dev", LocalDate.now());
        assertNotNull(emp.getId());
        assertEquals("DOC123", emp.getDocumentNumber());
        assertEquals(DocumentType.PA, emp.getDocumentType());
    }

    @Test
    void shouldCreateEmployeeWithNormalizedDocumentType() {
        Employee employee = Employee.create(
            Document.of("cc", "123"),
            "Ana",
            "Diaz",
            "ana@example.com",
            "Analyst",
            LocalDate.now()
        );

        assertEquals(DocumentType.CC, employee.getDocumentType());
        assertEquals("123", employee.getDocumentNumber());
    }

    @Test
    void shouldRejectDocumentNumbersThatDoNotMatchTheirType() {
        assertThrows(InvalidEmployeeDataException.class, () -> Document.of("CC", "12A"));
        assertThrows(InvalidEmployeeDataException.class, () -> Document.of("CE", "12"));
        assertThrows(InvalidEmployeeDataException.class, () -> Document.of("PA", "AB-123"));
        assertThrows(InvalidEmployeeDataException.class, () -> Document.of("XX", "123"));
    }

    @Test
    void shouldKeepDocumentAndHireDateImmutableOnUpdate() {
        LocalDate hireDate = LocalDate.of(2020, 1, 1);
        Employee employee = Employee.create(
            Document.of("CE", "12345"),
            "Ana",
            "Diaz",
            "ana@example.com",
            "Analyst",
            hireDate
        );

        assertThrows(InvalidEmployeeDataException.class, () -> employee.update(
            "12346", "Ana", "Diaz", "ana@example.com", "Analyst", hireDate
        ));
        assertThrows(InvalidEmployeeDataException.class, () -> employee.update(
            "12345", "Ana", "Diaz", "ana@example.com", "Analyst", hireDate.plusDays(1)
        ));
        employee.update("12345", "Ana", "Diaz", "ana@example.com", "Senior Analyst", hireDate);

        assertEquals(hireDate, employee.getHireDate());
        assertEquals("Senior Analyst", employee.getPosition());
    }
}