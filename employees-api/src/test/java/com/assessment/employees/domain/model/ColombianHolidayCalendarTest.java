package com.assessment.employees.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ColombianHolidayCalendarTest {
    @Test
    void shouldCalculateColombianHolidaysFor2026() {
        Set<LocalDate> holidays = ColombianHolidayCalendar.holidaysForYear(2026);

        assertTrue(holidays.contains(LocalDate.of(2026, 1, 1)));
        assertTrue(holidays.contains(LocalDate.of(2026, 1, 12)));
        assertTrue(holidays.contains(LocalDate.of(2026, 4, 2)));
        assertTrue(holidays.contains(LocalDate.of(2026, 4, 3)));
        assertTrue(holidays.contains(LocalDate.of(2026, 5, 1)));
        assertTrue(holidays.contains(LocalDate.of(2026, 5, 18)));
        assertTrue(holidays.contains(LocalDate.of(2026, 6, 8)));
        assertTrue(holidays.contains(LocalDate.of(2026, 6, 15)));
        assertTrue(holidays.contains(LocalDate.of(2026, 7, 20)));
        assertTrue(holidays.contains(LocalDate.of(2026, 8, 7)));
        assertTrue(holidays.contains(LocalDate.of(2026, 8, 17)));
        assertTrue(holidays.contains(LocalDate.of(2026, 10, 12)));
        assertTrue(holidays.contains(LocalDate.of(2026, 11, 2)));
        assertTrue(holidays.contains(LocalDate.of(2026, 11, 16)));
        assertTrue(holidays.contains(LocalDate.of(2026, 12, 8)));
        assertTrue(holidays.contains(LocalDate.of(2026, 12, 25)));
        assertFalse(holidays.contains(LocalDate.of(2026, 4, 5)));
        assertEquals(18, holidays.size());
    }
}
