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
    void shouldAcceptExactlyFortyTwoHoursPerIsoWeek() {
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
    void shouldRejectMoreThanFortyTwoHoursPerIsoWeek() {
        List<WorkShift> shifts = weekdayShifts(8);
        shifts.add(createShift(
            LocalDate.of(2026, 10, 18),
            LocalTime.of(21, 0),
            LocalTime.MIDNIGHT
        ));

        WeeklyHoursExceededException exception = assertThrows(
            WeeklyHoursExceededException.class,
            () -> WeeklyWorkloadPolicy.validate(shifts)
        );

        assertEquals(LocalDate.of(2026, 10, 12), exception.getWeekStart());
        assertEquals(Duration.ofHours(43), exception.getScheduledHours());
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
