package com.assessment.employees.domain.model;

import com.assessment.employees.domain.exception.InvalidEmployeeDataException;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class EmployeeTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 3, 1);

    @Test
    void shouldCreateEmployee() {
        Employee emp = Employee.create(Document.of("PA", "DOC123"), "John", "Doe",
            "john@test.com", "Dev", TODAY, TODAY);
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
            LocalDate.of(2020, 1, 3),
            TODAY
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
    void shouldRejectFutureHireDates() {
        assertThrows(InvalidEmployeeDataException.class, () -> Employee.create(
            Document.of("CC", "123456"),
            "Ana",
            "Diaz",
            "ana@example.com",
            "Analyst",
            TODAY.plusDays(1),
            TODAY
        ));
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
            hireDate,
            TODAY
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

    @Test
    void shouldUpdateOnlyProvidedFieldsAndKeepTheOthers() {
        Employee employee = Employee.create(
            Document.of("CC", "123456"),
            "Ana",
            "Diaz",
            "ana@example.com",
            "Analyst",
            LocalDate.of(2020, 1, 1),
            TODAY
        );

        employee.update(null, null, null, "Senior Analyst");

        assertEquals("Ana", employee.getFirstName());
        assertEquals("Diaz", employee.getLastName());
        assertEquals("ana@example.com", employee.getEmail().value());
        assertEquals("Senior Analyst", employee.getPosition());
    }

    @Test
    void shouldValidateOnlyProvidedFieldsOnPartialUpdate() {
        Employee employee = Employee.create(
            Document.of("CC", "123456"),
            "Ana",
            "Diaz",
            "ana@example.com",
            "Analyst",
            LocalDate.of(2020, 1, 1),
            TODAY
        );

        assertThrows(InvalidEmployeeDataException.class, () -> employee.update("", null, null, null));
        assertThrows(InvalidEmployeeDataException.class, () -> employee.update(null, "x".repeat(101), null, null));
        assertThrows(InvalidEmployeeDataException.class, () -> employee.update(null, null, "invalid-email", null));
        assertThrows(InvalidEmployeeDataException.class, () -> employee.update(null, null, null, "  "));

        employee.update("Ana Dos", null, "ana.dos@example.com", null);

        assertEquals("Ana Dos", employee.getFirstName());
        assertEquals("ana.dos@example.com", employee.getEmail().value());
        assertEquals("Diaz", employee.getLastName());
        assertEquals("Analyst", employee.getPosition());
    }
}
