package com.assessment.employees.domain.model;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
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

    /**
     * Los festivos derivados de la pascua (Ascensión, Corpus Christi y Sagrado
     * Corazón) se trasladan al lunes: 43/64/71 días desde el domingo de pascua
     * siempre caen en lunes. Fechas oficiales de referencia para dos años.
     */
    @Test
    void shouldPlaceEasterDerivedHolidaysOnMonday() {
        Set<LocalDate> holidays2023 = ColombianHolidayCalendar.holidaysForYear(2023);
        Set<LocalDate> holidays2028 = ColombianHolidayCalendar.holidaysForYear(2028);

        assertTrue(holidays2023.containsAll(List.of(
            LocalDate.of(2023, 5, 22), LocalDate.of(2023, 6, 12), LocalDate.of(2023, 6, 19))));
        assertTrue(holidays2028.containsAll(List.of(
            LocalDate.of(2028, 5, 29), LocalDate.of(2028, 6, 19), LocalDate.of(2028, 6, 26))));
        holidays2023.stream().filter(h -> List.of(5, 6).contains(h.getMonthValue()))
            .forEach(h -> assertEquals(DayOfWeek.MONDAY, h.getDayOfWeek(), h.toString()));
        holidays2028.stream().filter(h -> List.of(5, 6).contains(h.getMonthValue()))
            .forEach(h -> assertEquals(DayOfWeek.MONDAY, h.getDayOfWeek(), h.toString()));
    }
}
