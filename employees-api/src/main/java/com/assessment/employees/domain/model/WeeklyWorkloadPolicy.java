package com.assessment.employees.domain.model;

import com.assessment.employees.domain.exception.WeeklyHoursExceededException;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public final class WeeklyWorkloadPolicy {
    public static final Duration MAX_WEEKLY_HOURS = Duration.ofHours(42);

    private WeeklyWorkloadPolicy() {
    }

    public static Map<LocalDate, Duration> weeklyHours(Collection<WorkShift> workShifts) {
        Map<LocalDate, Duration> hoursByWeek = new TreeMap<>();
        for (WorkShift workShift : workShifts) {
            addShiftHours(hoursByWeek, workShift);
        }
        return Collections.unmodifiableMap(hoursByWeek);
    }

    public static void validate(Collection<WorkShift> workShifts) {
        weeklyHours(workShifts).forEach((weekStart, scheduledHours) -> {
            if (scheduledHours.compareTo(MAX_WEEKLY_HOURS) > 0) {
                throw new WeeklyHoursExceededException(weekStart, scheduledHours);
            }
        });
    }

    public static Map<LocalDate, Duration> exceededWeeks(Collection<WorkShift> existing,
                                                          Collection<WorkShift> additions) {
        Map<LocalDate, Duration> hoursByWeek = new TreeMap<>();
        existing.forEach(shift -> addShiftHours(hoursByWeek, shift));
        additions.forEach(shift -> addShiftHours(hoursByWeek, shift));
        Set<LocalDate> affectedWeeks = weeklyHours(additions).keySet();
        Map<LocalDate, Duration> exceeded = new TreeMap<>();
        affectedWeeks.forEach(weekStart -> {
            Duration hours = hoursByWeek.getOrDefault(weekStart, Duration.ZERO);
            if (hours.compareTo(MAX_WEEKLY_HOURS) > 0) {
                exceeded.put(weekStart, hours);
            }
        });
        return Collections.unmodifiableMap(exceeded);
    }

    public static void validateAdditions(Collection<WorkShift> existing,
                                         Collection<WorkShift> additions) {
        exceededWeeks(existing, additions).forEach((weekStart, hours) -> {
            throw new WeeklyHoursExceededException(weekStart, hours);
        });
    }

    private static void addShiftHours(Map<LocalDate, Duration> hoursByWeek,
                                      WorkShift workShift) {
        for (LocalDate weekStart = workShift.getWeekStartDate();
             !weekStart.isAfter(workShift.getEndDate());
             weekStart = weekStart.plusWeeks(1)) {
            Duration hours = workShift.getWorkedHoursWithin(
                weekStart.atStartOfDay(),
                weekStart.plusWeeks(1).atStartOfDay()
            );
            if (!hours.isZero()) {
                hoursByWeek.merge(weekStart, hours, Duration::plus);
            }
        }
    }
}
