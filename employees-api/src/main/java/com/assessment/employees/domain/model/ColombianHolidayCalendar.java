package com.assessment.employees.domain.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.temporal.TemporalAdjusters;
import java.util.HashSet;
import java.util.Set;

public final class ColombianHolidayCalendar {
    /**
     * Festivos con fecha fija y festivos Emiliani (se trasladan al lunes
     * siguiente), Ley 51/1983 y Ley 904/2004.
     */
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
    /** Jueves santo: pascua − 3 días. */
    private static final int HOLY_THURSDAY_OFFSET_DAYS = 3;
    /** Viernes santo: pascua − 2 días. */
    private static final int GOOD_FRIDAY_OFFSET_DAYS = 2;
    /** Ascensión del Señor: pascua + 43 días; 43 = 6×7 + 1 cae en lunes. */
    private static final int ASCENSION_OFFSET_DAYS = 43;
    /** Corpus Christi: pascua + 64 días; 64 = 9×7 + 1 cae en lunes. */
    private static final int CORPUS_CHRISTI_OFFSET_DAYS = 64;
    /** Sagrado Corazón: pascua + 71 días; 71 = 10×7 + 1 cae en lunes. */
    private static final int SACRED_HEART_OFFSET_DAYS = 71;

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
        holidays.add(easterSunday.minusDays(HOLY_THURSDAY_OFFSET_DAYS));
        holidays.add(easterSunday.minusDays(GOOD_FRIDAY_OFFSET_DAYS));
        holidays.add(easterSunday.plusDays(ASCENSION_OFFSET_DAYS));
        holidays.add(easterSunday.plusDays(CORPUS_CHRISTI_OFFSET_DAYS));
        holidays.add(easterSunday.plusDays(SACRED_HEART_OFFSET_DAYS));
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
