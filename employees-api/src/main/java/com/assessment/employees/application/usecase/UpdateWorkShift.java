package com.assessment.employees.application.usecase;

import com.assessment.employees.application.dto.UpdateWorkShiftCommand;
import com.assessment.employees.application.dto.WorkShiftResult;
import com.assessment.employees.domain.exception.EmployeeNotFoundException;
import com.assessment.employees.domain.exception.WorkShiftNotFoundException;
import com.assessment.employees.domain.exception.WorkShiftOverlapException;
import com.assessment.employees.domain.model.Document;
import com.assessment.employees.domain.model.WorkShift;
import com.assessment.employees.domain.model.WeeklyWorkloadPolicy;
import com.assessment.employees.domain.repository.EmployeeRepository;
import com.assessment.employees.domain.repository.WorkShiftRepository;

import java.util.List;
import java.util.UUID;

public class UpdateWorkShift {
    private final WorkShiftRepository workShiftRepository;
    private final EmployeeRepository employeeRepository;

    public UpdateWorkShift(WorkShiftRepository workShiftRepository,
                           EmployeeRepository employeeRepository) {
        this.workShiftRepository = workShiftRepository;
        this.employeeRepository = employeeRepository;
    }

    public WorkShiftResult execute(UpdateWorkShiftCommand command) {
        UUID id = command.id();
        Document document = Document.of(command.documentType(), command.documentNumber());
        WorkShift existing = workShiftRepository.findById(id)
            .filter(shift -> shift.getEmployeeDocument().equals(document))
            .orElseThrow(() -> new WorkShiftNotFoundException(id));
        if (employeeRepository.findByDocument(document).isEmpty()) {
            throw new EmployeeNotFoundException(document);
        }

        WorkShift updated = WorkShift.create(
            document, command.date(), command.startTime(), command.endTime()
        );
        List<WorkShift> otherShifts = findRelevantShifts(document, updated).stream()
            .filter(shift -> !shift.getId().equals(id))
            .toList();
        rejectOverlap(otherShifts, updated);
        validateWeeklyWorkload(otherShifts, updated);
        existing.update(command.date(), command.startTime(), command.endTime());
        return WorkShiftResult.fromDomain(workShiftRepository.save(existing));
    }

    private List<WorkShift> findRelevantShifts(Document document, WorkShift shift) {
        return workShiftRepository.findByEmployeeDocumentAndDateRange(
            document, shift.getWeekStartDate().minusDays(1), shift.getWeekStartDate().plusWeeks(2)
        );
    }

    private void rejectOverlap(List<WorkShift> shifts, WorkShift candidate) {
        if (shifts.stream().anyMatch(candidate::overlaps)) {
            throw new WorkShiftOverlapException(
                candidate.getDate(), candidate.getStartTime(), candidate.getEndTime()
            );
        }
    }

    private void validateWeeklyWorkload(List<WorkShift> shifts, WorkShift candidate) {
        WeeklyWorkloadPolicy.validateAdditions(shifts, List.of(candidate));
    }
}
