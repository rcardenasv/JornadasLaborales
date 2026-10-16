package com.assessment.employees.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ValueObjectsTest {
    @Test
    void dateSupportsComparisonsAndRejectsNull() {
        Date today = Date.of(LocalDate.now());
        Date tomorrow = Date.of(LocalDate.now().plusDays(1));
        assertTrue(today.isBefore(tomorrow));
        assertTrue(tomorrow.isAfter(today));
        assertTrue(today.isBeforeOrEqual(today));
        assertTrue(tomorrow.isAfterOrEqual(today));
        assertThrows(IllegalArgumentException.class, () -> new Date(null));
    }

    @Test
    void timeSupportsComparisonsAndMinuteDifference() {
        Time start = Time.of(9, 0);
        Time end = Time.of(LocalTime.of(10, 30));
        assertTrue(start.isBefore(end));
        assertTrue(end.isAfter(start));
        assertEquals(90, start.differenceInMinutes(end));
        assertThrows(IllegalArgumentException.class, () -> new Time(null));
    }

    @Test
    void emailAndDocumentNumberValidateInput() {
        assertEquals("ana@example.com", Email.of("ana@example.com").value());
        assertThrows(IllegalArgumentException.class, () -> Email.of("invalid"));
        assertThrows(IllegalArgumentException.class, () -> Email.of(" "));
        assertEquals("EMP001", DocumentNumber.of("EMP001").value());
        assertThrows(IllegalArgumentException.class, () -> DocumentNumber.of("12"));
        assertThrows(IllegalArgumentException.class, () -> DocumentNumber.of("EMP-001"));
    }

    @Test
    void employeeIdValidatesAndCreatesRandomIds() {
        UUID value = UUID.randomUUID();
        assertEquals(value, EmployeeId.of(value).value());
        assertNotNull(EmployeeId.random().value());
        assertThrows(IllegalArgumentException.class, () -> new EmployeeId(null));
    }
}
