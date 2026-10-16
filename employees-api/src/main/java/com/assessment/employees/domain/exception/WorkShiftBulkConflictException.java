package com.assessment.employees.domain.exception;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class WorkShiftBulkConflictException extends DomainException {
    private final List<LocalDate> conflictDates;

    public WorkShiftBulkConflictException(List<LocalDate> conflictDates) {
        super("No se crearon jornadas. Hay conflictos en las fechas: "
            + conflictDates.stream().map(LocalDate::toString).collect(Collectors.joining(", ")) + ".");
        this.conflictDates = List.copyOf(conflictDates);
    }

    public List<LocalDate> getConflictDates() {
        return conflictDates;
    }
}
