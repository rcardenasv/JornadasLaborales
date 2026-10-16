package com.assessment.employees.domain.exception;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;

/**
 * Semana ISO cuyo total programado supera el máximo semanal permitido
 * (límite ordinario vigente más el tope de horas extra). El exceso dentro del
 * tope no es error: se reporta como horas extra en los resúmenes semanales.
 */
public class WeeklyHoursExceededException extends DomainException {
    private final LocalDate weekStart;
    private final Duration scheduledHours;
    private final Duration maxAllowedHours;

    public WeeklyHoursExceededException(LocalDate weekStart,
                                        Duration scheduledHours,
                                        Duration maxAllowedHours) {
        super("La semana que inicia el " + weekStart + " programa " + formatHours(scheduledHours)
            + " horas y supera el máximo semanal permitido de " + formatHours(maxAllowedHours)
            + " horas");
        this.weekStart = weekStart;
        this.scheduledHours = scheduledHours;
        this.maxAllowedHours = maxAllowedHours;
    }

    public LocalDate getWeekStart() {
        return weekStart;
    }

    public Duration getScheduledHours() {
        return scheduledHours;
    }

    public Duration getMaxAllowedHours() {
        return maxAllowedHours;
    }

    private static String formatHours(Duration duration) {
        return BigDecimal.valueOf(duration.toMinutes())
            .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP)
            .toPlainString();
    }
}
