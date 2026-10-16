package com.assessment.employees.application.usecase;

import com.assessment.employees.application.dto.WeeklyWorkloadSummaryResult;
import com.assessment.employees.application.dto.WorkShiftListResult;
import com.assessment.employees.application.dto.WorkShiftResult;
import com.assessment.employees.domain.exception.EmployeeNotFoundException;
import com.assessment.employees.domain.exception.InvalidDateRangeException;
import com.assessment.employees.domain.model.Document;
import com.assessment.employees.domain.model.WeeklyWorkloadPolicy;
import com.assessment.employees.domain.model.WorkShift;
import com.assessment.employees.domain.repository.EmployeeRepository;
import com.assessment.employees.domain.repository.WorkShiftRepository;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.time.temporal.TemporalAdjusters;

/**
 * Use case for listing work shifts for an employee.
 */
public class ListEmployeeWorkShifts {
    private final WorkShiftRepository workShiftRepository;
    private final EmployeeRepository employeeRepository;
    
    public ListEmployeeWorkShifts(WorkShiftRepository workShiftRepository,
                                   EmployeeRepository employeeRepository) {
        this.workShiftRepository = workShiftRepository;
        this.employeeRepository = employeeRepository;
    }
    
    public WorkShiftListResult execute(String documentType, String documentNumber,
                                       LocalDate from, LocalDate to) {
        Document document = Document.of(documentType, documentNumber);
        if (employeeRepository.findByDocument(document).isEmpty()) {
            throw new EmployeeNotFoundException(document);
        }

        if (from != null && to != null && from.isAfter(to)) {
            throw new InvalidDateRangeException("La fecha desde no puede ser posterior a la fecha hasta.");
        }

        LocalDate queryFrom = from == null ? null : weekStart(from).minusDays(1);
        LocalDate queryTo = to == null ? null : weekEnd(to);
        List<WorkShift> shifts = from == null && to == null
            ? workShiftRepository.findByEmployeeDocument(document)
            : workShiftRepository.findByEmployeeDocumentAndDateRange(document, queryFrom, queryTo);

        List<WorkShiftResult> results = shifts.stream()
            .filter(shift -> from == null || !shift.getDate().isBefore(from))
            .filter(shift -> to == null || !shift.getDate().isAfter(to))
            .map(WorkShiftResult::fromDomain)
            .toList();
        List<WeeklyWorkloadSummaryResult> weeklySummaries = WeeklyWorkloadPolicy.weeklyHours(shifts)
            .entrySet()
            .stream()
            .filter(entry -> from == null || !entry.getKey().isBefore(weekStart(from)))
            .filter(entry -> to == null || !entry.getKey().isAfter(weekStart(to)))
            .map(entry -> toSummary(entry.getKey(), entry.getValue()))
            .toList();
        return new WorkShiftListResult(results, weeklySummaries);
    }

    private WeeklyWorkloadSummaryResult toSummary(LocalDate weekStart, Duration scheduledHours) {
        return new WeeklyWorkloadSummaryResult(
            weekStart,
            scheduledHours,
            WeeklyWorkloadPolicy.ordinaryLimitHours(weekStart),
            WeeklyWorkloadPolicy.overtimeHours(scheduledHours, weekStart),
            WeeklyWorkloadPolicy.remainingOrdinaryHours(scheduledHours, weekStart)
        );
    }

    private LocalDate weekStart(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private LocalDate weekEnd(LocalDate date) {
        return date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    }
}