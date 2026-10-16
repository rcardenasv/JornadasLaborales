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
