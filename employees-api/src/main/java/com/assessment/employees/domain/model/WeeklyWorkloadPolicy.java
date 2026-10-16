package com.assessment.employees.domain.model;

import com.assessment.employees.domain.exception.WeeklyHoursExceededException;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

/**
 * Límite semanal ordinario por vigencia (Ley 2101 de 2021) y tope de horas extra
 * (art. 22 de la Ley 50 de 1990, modificado por la Ley 2466 de 2025).
 * El límite de una semana ISO es el vigente en su domingo (criterio conservador).
 * El exceso sobre el límite ordinario se reporta como horas extra: solo se rechaza
 * cuando las extra de la semana superan MAX_OVERTIME_WEEKLY_HOURS.
 */
public final class WeeklyWorkloadPolicy {
    public static final Duration MAX_OVERTIME_WEEKLY_HOURS = Duration.ofHours(12);

    private static final Duration PRE_REDUCTION_WEEKLY_LIMIT = Duration.ofHours(48);
    private static final LocalDate REDUCTION_START = LocalDate.of(2023, 7, 15);
    private static final NavigableMap<LocalDate, Duration> WEEKLY_LIMIT_BY_EFFECTIVE_DATE;

    static {
        NavigableMap<LocalDate, Duration> vigencias = new TreeMap<>();
        vigencias.put(REDUCTION_START, Duration.ofHours(47));
        vigencias.put(LocalDate.of(2024, 7, 15), Duration.ofHours(46));
        vigencias.put(LocalDate.of(2025, 7, 15), Duration.ofHours(44));
        vigencias.put(LocalDate.of(2026, 7, 15), Duration.ofHours(42));
        WEEKLY_LIMIT_BY_EFFECTIVE_DATE = Collections.unmodifiableNavigableMap(vigencias);
    }

    private WeeklyWorkloadPolicy() {
    }

    public static Duration ordinaryLimitHours(LocalDate weekStart) {
        LocalDate weekSunday = weekStart.plusDays(6);
        Map.Entry<LocalDate, Duration> vigencia =
            WEEKLY_LIMIT_BY_EFFECTIVE_DATE.floorEntry(weekSunday);
        return vigencia == null ? PRE_REDUCTION_WEEKLY_LIMIT : vigencia.getValue();
    }

    public static Duration overtimeHours(Duration scheduledHours, LocalDate weekStart) {
        Duration excess = scheduledHours.minus(ordinaryLimitHours(weekStart));
        return excess.isNegative() ? Duration.ZERO : excess;
    }

    public static Duration remainingOrdinaryHours(Duration scheduledHours, LocalDate weekStart) {
        Duration remaining = ordinaryLimitHours(weekStart).minus(scheduledHours);
        return remaining.isNegative() ? Duration.ZERO : remaining;
    }

    public static Map<LocalDate, Duration> weeklyHours(Collection<WorkShift> workShifts) {
        Map<LocalDate, Duration> hoursByWeek = new TreeMap<>();
        for (WorkShift workShift : workShifts) {
            addShiftHours(hoursByWeek, workShift);
        }
        return Collections.unmodifiableMap(hoursByWeek);
    }

    public static void validate(Collection<WorkShift> workShifts) {
        weeklyHours(workShifts).forEach(WeeklyWorkloadPolicy::rejectIfExceedsOvertimeCap);
    }

    public static Map<LocalDate, Duration> exceededWeeks(Collection<WorkShift> existing,
                                                          Collection<WorkShift> additions) {
        Map<LocalDate, Duration> hoursByWeek = new TreeMap<>();
        existing.forEach(shift -> addShiftHours(hoursByWeek, shift));
        additions.forEach(shift -> addShiftHours(hoursByWeek, shift));
        Set<LocalDate> affectedWeeks = weeklyHours(additions).keySet();
        Map<LocalDate, Duration> exceeded = new TreeMap<>();
        affectedWeeks.forEach(weekStart -> {
            if (hasOvertimeCapExceeded(hoursByWeek.getOrDefault(weekStart, Duration.ZERO), weekStart)) {
                exceeded.put(weekStart, hoursByWeek.get(weekStart));
            }
        });
        return Collections.unmodifiableMap(exceeded);
    }

    public static void validateAdditions(Collection<WorkShift> existing,
                                         Collection<WorkShift> additions) {
        exceededWeeks(existing, additions).forEach(WeeklyWorkloadPolicy::rejectIfExceedsOvertimeCap);
    }

    private static void rejectIfExceedsOvertimeCap(LocalDate weekStart, Duration scheduledHours) {
        if (hasOvertimeCapExceeded(scheduledHours, weekStart)) {
            throw new WeeklyHoursExceededException(weekStart, scheduledHours,
                ordinaryLimitHours(weekStart).plus(MAX_OVERTIME_WEEKLY_HOURS));
        }
    }

    private static boolean hasOvertimeCapExceeded(Duration scheduledHours, LocalDate weekStart) {
        return overtimeHours(scheduledHours, weekStart).compareTo(MAX_OVERTIME_WEEKLY_HOURS) > 0;
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
