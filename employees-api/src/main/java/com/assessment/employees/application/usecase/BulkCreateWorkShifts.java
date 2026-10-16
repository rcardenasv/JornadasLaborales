package com.assessment.employees.application.usecase;

import com.assessment.employees.application.dto.BulkCreateWorkShiftsCommand;
import com.assessment.employees.application.dto.BulkCreateWorkShiftsResult;
import com.assessment.employees.application.dto.WorkShiftResult;
import com.assessment.employees.domain.exception.EmployeeNotFoundException;
import com.assessment.employees.domain.exception.InvalidDateRangeException;
import com.assessment.employees.domain.exception.WorkShiftBulkConflictException;
import com.assessment.employees.domain.model.Document;
import com.assessment.employees.domain.model.WorkShift;
import com.assessment.employees.domain.model.WeeklyWorkloadPolicy;
import com.assessment.employees.domain.repository.EmployeeRepository;
import com.assessment.employees.domain.repository.WorkShiftRepository;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class BulkCreateWorkShifts {
    private static final long MAX_RANGE_DAYS = 92;

    private final WorkShiftRepository workShiftRepository;
    private final EmployeeRepository employeeRepository;

    public BulkCreateWorkShifts(WorkShiftRepository workShiftRepository,
                                EmployeeRepository employeeRepository) {
        this.workShiftRepository = workShiftRepository;
        this.employeeRepository = employeeRepository;
    }

    public BulkCreateWorkShiftsResult execute(BulkCreateWorkShiftsCommand command) {
        validateRange(command.from(), command.to());
        Set<DayOfWeek> weekdays = weekdays(command.weekdays());
        Document document = Document.of(command.documentType(), command.documentNumber());
        if (employeeRepository.findByDocument(document).isEmpty()) {
            throw new EmployeeNotFoundException(document);
        }

        List<WorkShift> additions = generateShifts(document, command, weekdays);
        if (additions.isEmpty()) {
            throw new InvalidDateRangeException("El rango no genera jornadas para los días seleccionados.");
        }

        List<WorkShift> existing = findRelevantShifts(document, command.from(), command.to());
        Set<LocalDate> conflicts = findOverlaps(existing, additions);
        conflicts.addAll(weeklyConflicts(existing, additions));
        if (!conflicts.isEmpty()) {
            throw new WorkShiftBulkConflictException(List.copyOf(conflicts));
        }

        List<WorkShift> saved = workShiftRepository.saveAll(additions);
        List<WorkShiftResult> results = saved.stream().map(WorkShiftResult::fromDomain).toList();
        Duration scheduledHours = results.stream()
            .map(WorkShiftResult::workedHours)
            .reduce(Duration.ZERO, Duration::plus);
        return new BulkCreateWorkShiftsResult(results, scheduledHours);
    }

    private void validateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null || from.isAfter(to)) {
            throw new InvalidDateRangeException("El rango de fechas es obligatorio y debe ser válido.");
        }
        if (ChronoUnit.DAYS.between(from, to) + 1 > MAX_RANGE_DAYS) {
            throw new InvalidDateRangeException("El rango de carga no puede superar 92 días.");
        }
    }

    private Set<DayOfWeek> weekdays(Set<DayOfWeek> requested) {
        if (requested != null && requested.isEmpty()) {
            throw new InvalidDateRangeException("Debe seleccionar al menos un día de la semana.");
        }
        return requested == null ? Set.of(DayOfWeek.values()) : Set.copyOf(requested);
    }

    private List<WorkShift> generateShifts(Document document, BulkCreateWorkShiftsCommand command,
                                           Set<DayOfWeek> weekdays) {
        List<WorkShift> shifts = new ArrayList<>();
        for (LocalDate date = command.from(); !date.isAfter(command.to()); date = date.plusDays(1)) {
            if (weekdays.contains(date.getDayOfWeek())) {
                shifts.add(WorkShift.create(document, date, command.startTime(), command.endTime()));
            }
        }
        return shifts;
    }

    private List<WorkShift> findRelevantShifts(Document document, LocalDate from, LocalDate to) {
        LocalDate firstWeek = WorkShift.weekStartOf(from);
        LocalDate lastWeek = WorkShift.weekStartOf(to);
        return workShiftRepository.findByEmployeeDocumentAndDateRange(
            document, firstWeek.minusDays(1), lastWeek.plusWeeks(1).plusDays(6)
        );
    }

    private Set<LocalDate> findOverlaps(List<WorkShift> existing, List<WorkShift> additions) {
        Set<LocalDate> conflicts = new TreeSet<>();
        for (int index = 0; index < additions.size(); index++) {
            WorkShift candidate = additions.get(index);
            if (existing.stream().anyMatch(candidate::overlaps)) {
                conflicts.add(candidate.getDate());
            }
            for (int otherIndex = index + 1; otherIndex < additions.size(); otherIndex++) {
                WorkShift other = additions.get(otherIndex);
                if (candidate.overlaps(other)) {
                    conflicts.add(candidate.getDate());
                    conflicts.add(other.getDate());
                }
            }
        }
        return conflicts;
    }

    private Set<LocalDate> weeklyConflicts(List<WorkShift> existing, List<WorkShift> additions) {
        Set<LocalDate> conflicts = new TreeSet<>();
        WeeklyWorkloadPolicy.exceededWeeks(existing, additions).forEach((weekStart, hours) -> {
            additions.stream()
                .filter(shift -> !shift.getWorkedHoursWithin(
                    weekStart.atStartOfDay(), weekStart.plusWeeks(1).atStartOfDay()
                ).isZero())
                .map(WorkShift::getDate)
                .forEach(conflicts::add);
        });
        return conflicts;
    }
}
