package com.assessment.employees.domain.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.temporal.TemporalAdjusters;
import java.util.HashSet;
import java.util.Set;

public final class ColombianHolidayCalendar {
    private static final Set<MonthDay> FIXED_HOLIDAYS = Set.of(
        MonthDay.of(1, 1),
        MonthDay.of(5, 1),
        MonthDay.of(7, 20),
        MonthDay.of(8, 7),
        MonthDay.of(12, 8),
        MonthDay.of(12, 25)
    );
    private static final Set<MonthDay> EMILIANI_HOLIDAYS = Set.of(
        MonthDay.of(1, 6),
        MonthDay.of(3, 19),
        MonthDay.of(6, 29),
        MonthDay.of(8, 15),
        MonthDay.of(10, 12),
        MonthDay.of(11, 1),
        MonthDay.of(11, 16)
    );

    private ColombianHolidayCalendar() {
    }

    public static boolean isHoliday(LocalDate date) {
        return holidaysForYear(date.getYear()).contains(date);
    }

    public static Set<LocalDate> holidaysForYear(int year) {
        Set<LocalDate> holidays = new HashSet<>();
        addFixedHolidays(year, holidays);
        addEmilianiHolidays(year, holidays);
        addEasterHolidays(year, holidays);
        return Set.copyOf(holidays);
    }

    private static void addFixedHolidays(int year, Set<LocalDate> holidays) {
        FIXED_HOLIDAYS.stream()
            .map(monthDay -> monthDay.atYear(year))
            .forEach(holidays::add);
    }

    private static void addEmilianiHolidays(int year, Set<LocalDate> holidays) {
        EMILIANI_HOLIDAYS.stream()
            .map(monthDay -> nextMonday(monthDay.atYear(year)))
            .forEach(holidays::add);
    }

    private static void addEasterHolidays(int year, Set<LocalDate> holidays) {
        LocalDate easterSunday = calculateEasterSunday(year);
        holidays.add(easterSunday.minusDays(3));
        holidays.add(easterSunday.minusDays(2));
        holidays.add(easterSunday.plusDays(43));
        holidays.add(easterSunday.plusDays(64));
        holidays.add(easterSunday.plusDays(71));
    }

    private static LocalDate nextMonday(LocalDate date) {
        return date.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
    }

    private static LocalDate calculateEasterSunday(int year) {
        int a = year % 19;
        int b = year / 100;
        int c = year % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int month = (h + l - 7 * m + 114) / 31;
        int day = (h + l - 7 * m + 114) % 31 + 1;
        return LocalDate.of(year, month, day);
    }
}
