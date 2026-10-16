package com.assessment.employees.domain.model;

import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkShiftTest {
    @Test
    void shouldCalculateDaytimeHoursForDayShift() {
        Document empId = Document.of("CC", "123456");
        WorkShift shift = WorkShift.create(
            empId,
            LocalDate.of(2026, 10, 16),
            LocalTime.of(8, 0),
            LocalTime.of(17, 0)
        );

        assertEquals(Duration.ofHours(9), shift.getWorkedHours());
        assertEquals(Duration.ofHours(9), shift.getDaytimeHours());
        assertEquals(Duration.ZERO, shift.getNighttimeHours());
        assertEquals(ShiftType.DIURNA, shift.getShiftType());
        assertEquals(DayType.ORDINARIO, shift.getDayType());
        assertTrue(shift.getStartTime().isBefore(shift.getEndTime()));
    }

    @Test
    void shouldClassifyNighttimeAndMixedShifts() {
        Document employeeId = Document.of("CC", "123456");
        WorkShift nighttime = WorkShift.create(
            employeeId,
            LocalDate.of(2026, 10, 16),
            LocalTime.of(22, 0),
            LocalTime.of(6, 0)
        );
        WorkShift mixed = WorkShift.create(
            employeeId,
            LocalDate.of(2025, 12, 24),
            LocalTime.of(5, 0),
            LocalTime.of(23, 0)
        );

        assertEquals(Duration.ZERO, nighttime.getDaytimeHours());
        assertEquals(Duration.ofHours(8), nighttime.getWorkedHours());
        assertEquals(Duration.ofHours(8), nighttime.getNighttimeHours());
        assertEquals(ShiftType.NOCTURNA, nighttime.getShiftType());
        assertEquals(Duration.ofHours(15), mixed.getDaytimeHours());
        assertEquals(Duration.ofHours(3), mixed.getNighttimeHours());
        assertEquals(ShiftType.MIXTA, mixed.getShiftType());
    }

    @Test
    void shouldSplitDayAndNightHoursUsingWindowEffectiveAtStartDate() {
        Document employeeId = Document.of("CC", "123456");
        WorkShift beforeLawChange = WorkShift.create(
            employeeId,
            LocalDate.of(2025, 12, 24),
            LocalTime.of(18, 0),
            LocalTime.of(22, 0)
        );
        WorkShift onLawChange = WorkShift.create(
            employeeId,
            LocalDate.of(2025, 12, 25),
            LocalTime.of(18, 0),
            LocalTime.of(22, 0)
        );

        assertEquals(Duration.ofHours(3), beforeLawChange.getDaytimeHours());
        assertEquals(Duration.ofHours(1), beforeLawChange.getNighttimeHours());
        assertEquals(Duration.ofHours(1), onLawChange.getDaytimeHours());
        assertEquals(Duration.ofHours(3), onLawChange.getNighttimeHours());
    }

    @Test
    void shouldApplyStartDateWindowToShiftsCrossingMidnight() {
        WorkShift overnight = WorkShift.create(
            Document.of("CC", "123456"),
            LocalDate.of(2025, 12, 25),
            LocalTime.of(18, 0),
            LocalTime.of(2, 0)
        );

        assertEquals(Duration.ofHours(1), overnight.getDaytimeHours());
        assertEquals(Duration.ofHours(7), overnight.getNighttimeHours());
    }

    @Test
    void shouldClassifySundayAndHolidayByStartDate() {
        Document employeeId = Document.of("CC", "123456");
        WorkShift sunday = WorkShift.create(
            employeeId,
            LocalDate.of(2026, 1, 11),
            LocalTime.of(8, 0),
            LocalTime.of(9, 0)
        );
        WorkShift holiday = WorkShift.create(
            employeeId,
            LocalDate.of(2026, 1, 12),
            LocalTime.of(8, 0),
            LocalTime.of(9, 0)
        );

        assertEquals(DayType.DOMINICAL, sunday.getDayType());
        assertEquals(DayType.FESTIVO, holiday.getDayType());
    }

    @Test
    void shouldRecalculateClassificationWhenUpdated() {
        WorkShift shift = WorkShift.create(
            Document.of("CC", "123456"),
            LocalDate.of(2026, 10, 16),
            LocalTime.of(8, 0),
            LocalTime.of(17, 0)
        );

        shift.update(
            LocalDate.of(2026, 1, 11),
            LocalTime.of(22, 0),
            LocalTime.of(23, 0)
        );

        assertEquals(DayType.DOMINICAL, shift.getDayType());
        assertEquals(ShiftType.NOCTURNA, shift.getShiftType());
        assertEquals(Duration.ZERO, shift.getDaytimeHours());
        assertEquals(Duration.ofHours(1), shift.getNighttimeHours());
    }

    @Test
    void shouldAllowOvernightShiftsAndRejectZeroDuration() {
        Document employeeId = Document.of("CC", "123456");
        WorkShift overnight = WorkShift.create(
            employeeId,
            LocalDate.of(2026, 10, 16),
            LocalTime.of(22, 0),
            LocalTime.of(6, 0)
        );

        assertEquals(LocalDate.of(2026, 10, 17), overnight.getEndDate());
        assertEquals(Duration.ofHours(8), overnight.getWorkedHours());
        assertThrows(IllegalArgumentException.class, () -> WorkShift.create(
            employeeId,
            LocalDate.of(2026, 10, 16),
            LocalTime.of(8, 0),
            LocalTime.of(8, 0)
        ));
    }

    @Test
    void shouldDetectOverlapsAcrossMidnightUsingRealIntervals() {
        Document employeeId = Document.of("CC", "123456");
        WorkShift overnight = WorkShift.create(
            employeeId,
            LocalDate.of(2026, 10, 16),
            LocalTime.of(22, 0),
            LocalTime.of(6, 0)
        );
        WorkShift overlapping = WorkShift.create(
            employeeId,
            LocalDate.of(2026, 10, 17),
            LocalTime.of(5, 0),
            LocalTime.of(7, 0)
        );
        WorkShift adjacent = WorkShift.create(
            employeeId,
            LocalDate.of(2026, 10, 17),
            LocalTime.of(6, 0),
            LocalTime.of(8, 0)
        );

        assertTrue(overnight.overlaps(overlapping));
        assertFalse(overnight.overlaps(adjacent));
    }
}