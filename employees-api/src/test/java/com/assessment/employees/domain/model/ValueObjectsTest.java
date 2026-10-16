package com.assessment.employees.domain.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ValueObjectsTest {
    @Test
    void emailValidatesInput() {
        assertEquals("ana@example.com", Email.of("ana@example.com").value());
        assertThrows(IllegalArgumentException.class, () -> Email.of("invalid"));
        assertThrows(IllegalArgumentException.class, () -> Email.of(" "));
    }

    @Test
    void employeeIdValidatesAndCreatesRandomIds() {
        UUID value = UUID.randomUUID();
        assertEquals(value, EmployeeId.of(value).value());
        assertNotNull(EmployeeId.random().value());
        assertThrows(IllegalArgumentException.class, () -> new EmployeeId(null));
    }
}
