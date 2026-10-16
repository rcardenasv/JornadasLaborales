package com.assessment.employees.domain.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Ventana diurna/nocturna vigente en Colombia, parametrizada por la fecha de la jornada.
 * Antes de 2025-12-25: diurna 06:00-21:00 y nocturna 21:00-06:00.
 * Desde 2025-12-25 (Ley 2466 de 2025, art. 10): diurna 06:00-19:00 y nocturna 19:00-06:00.
 * La ventana de una jornada completa es la vigente en su fecha de inicio, incluso si cruza medianoche.
 */
public final class DayNightPolicy {
    private static final LocalTime DAYTIME_START = LocalTime.of(6, 0);
    private static final LocalTime LEGACY_DAYTIME_END = LocalTime.of(21, 0);
    private static final LocalTime CURRENT_DAYTIME_END = LocalTime.of(19, 0);
    private static final LocalDate LAW_2466_OF_2025_EFFECTIVE_DATE = LocalDate.of(2025, 12, 25);

    private DayNightPolicy() {
    }

    public static LocalTime daytimeStart(LocalDate shiftDate) {
        return DAYTIME_START;
    }

    public static LocalTime daytimeEnd(LocalDate shiftDate) {
        return isLaw2466Of2025Effective(shiftDate) ? CURRENT_DAYTIME_END : LEGACY_DAYTIME_END;
    }

    public static LocalTime nighttimeStart(LocalDate shiftDate) {
        return daytimeEnd(shiftDate);
    }

    private static boolean isLaw2466Of2025Effective(LocalDate shiftDate) {
        return !shiftDate.isBefore(LAW_2466_OF_2025_EFFECTIVE_DATE);
    }
}
