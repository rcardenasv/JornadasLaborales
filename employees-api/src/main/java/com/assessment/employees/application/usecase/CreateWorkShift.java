package com.assessment.employees.application.usecase;

import com.assessment.employees.application.dto.CreateWorkShiftCommand;
import com.assessment.employees.application.dto.WorkShiftResult;
import com.assessment.employees.domain.exception.EmployeeNotFoundException;
import com.assessment.employees.domain.model.Document;
import com.assessment.employees.domain.model.WorkShift;
import com.assessment.employees.domain.model.WeeklyWorkloadPolicy;
import com.assessment.employees.domain.repository.EmployeeRepository;
import com.assessment.employees.domain.repository.WorkShiftRepository;

import java.time.LocalDate;
import java.util.List;

public class CreateWorkShift {
    private final WorkShiftRepository workShiftRepository;
    private final EmployeeRepository employeeRepository;

    public CreateWorkShift(WorkShiftRepository workShiftRepository,
                           EmployeeRepository employeeRepository) {
        this.workShiftRepository = workShiftRepository;
        this.employeeRepository = employeeRepository;
    }

    public WorkShiftResult execute(CreateWorkShiftCommand command) {
        Document document = Document.of(command.documentType(), command.documentNumber());
        if (employeeRepository.findByDocument(document).isEmpty()) {
            throw new EmployeeNotFoundException(document);
        }

        WorkShift candidate = WorkShift.create(
            document, command.date(), command.startTime(), command.endTime()
        );
        List<WorkShift> existing = findRelevantShifts(document, candidate);
        WorkShift.rejectOverlap(existing, candidate);
        WeeklyWorkloadPolicy.validateAdditions(existing, List.of(candidate));
        return WorkShiftResult.fromDomain(workShiftRepository.save(candidate));
    }

    List<WorkShift> findRelevantShifts(Document document, WorkShift candidate) {
        LocalDate weekStart = candidate.getWeekStartDate();
        return workShiftRepository.findByEmployeeDocumentAndDateRange(
            document, weekStart.minusDays(1), weekStart.plusWeeks(2)
        );
    }
}
