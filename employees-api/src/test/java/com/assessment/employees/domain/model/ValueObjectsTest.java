package com.assessment.employees.domain.model;

import com.assessment.employees.domain.exception.InvalidEmployeeDataException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ValueObjectsTest {
    @Test
    void emailValidatesInput() {
        assertEquals("ana@example.com", Email.of("ana@example.com").value());
        assertEquals("El correo electrónico es obligatorio.",
            assertThrows(InvalidEmployeeDataException.class, () -> Email.of("  ")).getMessage());
        assertEquals("El correo electrónico no tiene un formato válido.",
            assertThrows(InvalidEmployeeDataException.class, () -> Email.of("invalid")).getMessage());
    }

    @Test
    void employeeIdValidatesAndCreatesRandomIds() {
        UUID value = UUID.randomUUID();
        assertEquals(value, EmployeeId.of(value).value());
        assertNotNull(EmployeeId.random().value());
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, () -> new EmployeeId(null));
        assertEquals("El identificador del empleado es obligatorio.", exception.getMessage());
    }
}
