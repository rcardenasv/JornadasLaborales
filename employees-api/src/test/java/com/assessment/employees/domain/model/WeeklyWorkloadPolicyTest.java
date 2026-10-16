package com.assessment.employees.domain.model;

import com.assessment.employees.domain.exception.WeeklyHoursExceededException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WeeklyWorkloadPolicyTest {
    @Test
    void shouldSplitOvernightHoursAtIsoWeekBoundary() {
        WorkShift shift = createShift(
            LocalDate.of(2026, 10, 18),
            LocalTime.of(22, 0),
            LocalTime.of(6, 0)
        );

        Map<LocalDate, Duration> weeklyHours =
            WeeklyWorkloadPolicy.weeklyHours(List.of(shift));

        assertEquals(Duration.ofHours(2), weeklyHours.get(LocalDate.of(2026, 10, 12)));
        assertEquals(Duration.ofHours(6), weeklyHours.get(LocalDate.of(2026, 10, 19)));
    }

    @Test
    void shouldUseWeeklyLimitEffectiveOnTheWeekSunday() {
        assertEquals(Duration.ofHours(47),
            WeeklyWorkloadPolicy.ordinaryLimitHours(LocalDate.of(2024, 1, 15)));
        assertEquals(Duration.ofHours(47),
            WeeklyWorkloadPolicy.ordinaryLimitHours(LocalDate.of(2024, 7, 8)));
        assertEquals(Duration.ofHours(46),
            WeeklyWorkloadPolicy.ordinaryLimitHours(LocalDate.of(2024, 7, 15)));
        assertEquals(Duration.ofHours(44),
            WeeklyWorkloadPolicy.ordinaryLimitHours(LocalDate.of(2025, 7, 14)));
        assertEquals(Duration.ofHours(42),
            WeeklyWorkloadPolicy.ordinaryLimitHours(LocalDate.of(2026, 7, 13)));
        assertEquals(Duration.ofHours(42),
            WeeklyWorkloadPolicy.ordinaryLimitHours(LocalDate.of(2026, 10, 12)));
    }

    @Test
    void shouldApplyFortyEightHourLimitBeforeReduction() {
        assertEquals(Duration.ofHours(48),
            WeeklyWorkloadPolicy.ordinaryLimitHours(LocalDate.of(2023, 6, 19)));
        assertEquals(Duration.ofHours(47),
            WeeklyWorkloadPolicy.ordinaryLimitHours(LocalDate.of(2023, 7, 17)));
    }

    @Test
    void shouldAcceptWeeklyHoursWithinOrdinaryLimitPlusOvertimeCap() {
        List<WorkShift> shifts = weekdayShifts(8);
        shifts.add(createShift(
            LocalDate.of(2026, 10, 18),
            LocalTime.of(22, 0),
            LocalTime.MIDNIGHT
        ));

        WeeklyWorkloadPolicy.validate(shifts);

        assertEquals(Duration.ofHours(42),
            WeeklyWorkloadPolicy.weeklyHours(shifts).get(LocalDate.of(2026, 10, 12)));
    }

    @Test
    void shouldAllowFortySixScheduledHoursInTwentyTwentySixAsFourOvertime() {
        List<WorkShift> shifts = weekdayShifts(8);
        shifts.add(createShift(LocalDate.of(2026, 10, 17), LocalTime.of(9, 0), LocalTime.of(15, 0)));

        WeeklyWorkloadPolicy.validate(shifts);

        weekTotal(shifts, LocalDate.of(2026, 10, 12), Duration.ofHours(46));
        assertEquals(Duration.ofHours(4), WeeklyWorkloadPolicy.overtimeHours(
            Duration.ofHours(46), LocalDate.of(2026, 10, 12)));
        assertEquals(Duration.ZERO, WeeklyWorkloadPolicy.remainingOrdinaryHours(
            Duration.ofHours(46), LocalDate.of(2026, 10, 12)));
    }

    @Test
    void shouldAcceptExactlyTwelveOvertimeHoursPerIsoWeek() {
        List<WorkShift> shifts = weekdayShifts(10);
        shifts.add(createShift(LocalDate.of(2026, 10, 17), LocalTime.of(8, 0), LocalTime.of(12, 0)));

        WeeklyWorkloadPolicy.validate(shifts);

        weekTotal(shifts, LocalDate.of(2026, 10, 12), Duration.ofHours(54));
    }

    @Test
    void shouldRejectWeeksExceedingOvertimeCap() {
        List<WorkShift> shifts = weekdayShifts(10);
        shifts.add(createShift(LocalDate.of(2026, 10, 17), LocalTime.of(8, 0), LocalTime.of(13, 0)));

        WeeklyHoursExceededException exception = assertThrows(
            WeeklyHoursExceededException.class,
            () -> WeeklyWorkloadPolicy.validate(shifts)
        );

        assertEquals(LocalDate.of(2026, 10, 12), exception.getWeekStart());
        assertEquals(Duration.ofHours(55), exception.getScheduledHours());
        assertEquals(Duration.ofHours(54), exception.getMaxAllowedHours());
    }

    @Test
    void shouldRejectAdditionsOnlyWhenOvertimeCapIsExceeded() {
        List<WorkShift> existing = weekdayShifts(10);
        List<WorkShift> additions = List.of(
            createShift(LocalDate.of(2026, 10, 17), LocalTime.of(8, 0), LocalTime.of(12, 0)));

        WeeklyWorkloadPolicy.validateAdditions(existing, additions);
    }

    private static void weekTotal(List<WorkShift> shifts, LocalDate weekStart, Duration expected) {
        assertEquals(expected, WeeklyWorkloadPolicy.weeklyHours(shifts).get(weekStart));
    }

    private static List<WorkShift> weekdayShifts(int hoursPerDay) {
        List<WorkShift> shifts = new ArrayList<>();
        for (int day = 12; day <= 16; day++) {
            shifts.add(createShift(
                LocalDate.of(2026, 10, day),
                LocalTime.of(8, 0),
                LocalTime.of(8 + hoursPerDay, 0)
            ));
        }
        return shifts;
    }

    private static WorkShift createShift(LocalDate date, LocalTime start, LocalTime end) {
        return WorkShift.create(Document.of("CC", "123456"), date, start, end);
    }
}
