package com.assessment.employees.domain.exception;

import java.time.Duration;
import java.time.LocalDate;

public class WeeklyHoursExceededException extends DomainException {
    private final LocalDate weekStart;
    private final Duration scheduledHours;

    public WeeklyHoursExceededException(LocalDate weekStart, Duration scheduledHours) {
        super("La semana que inicia el " + weekStart + " supera el límite de 42 horas");
        this.weekStart = weekStart;
        this.scheduledHours = scheduledHours;
    }

    public LocalDate getWeekStart() {
        return weekStart;
    }

    public Duration getScheduledHours() {
        return scheduledHours;
    }
}
