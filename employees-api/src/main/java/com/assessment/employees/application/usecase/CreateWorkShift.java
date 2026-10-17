package com.assessment.employees.application.usecase;

import com.assessment.employees.application.dto.CreateWorkShiftCommand;
import com.assessment.employees.application.dto.WorkShiftResult;
import com.assessment.employees.domain.model.Document;
import com.assessment.employees.domain.model.WorkShift;
import com.assessment.employees.domain.model.WeeklyWorkloadPolicy;
import com.assessment.employees.domain.repository.EmployeeRepository;
import com.assessment.employees.domain.repository.WorkShiftRepository;

import java.util.List;

public class CreateWorkShift {
    private final WorkShiftRepository workShiftRepository;
    private final EmployeeWorkShiftQueries queries;

    public CreateWorkShift(WorkShiftRepository workShiftRepository,
                           EmployeeRepository employeeRepository) {
        this.workShiftRepository = workShiftRepository;
        this.queries = new EmployeeWorkShiftQueries(employeeRepository, workShiftRepository);
    }

    public WorkShiftResult execute(CreateWorkShiftCommand command) {
        Document document = command.document();
        queries.requireExistingEmployee(document);

        WorkShift candidate = WorkShift.create(
            document, command.date(), command.startTime(), command.endTime()
        );
        List<WorkShift> existing = queries.shiftsAroundWeekOf(document, candidate.getDate());
        WorkShift.rejectOverlap(existing, candidate);
        WeeklyWorkloadPolicy.validateAdditions(existing, List.of(candidate));
        return WorkShiftResult.fromDomain(workShiftRepository.save(candidate));
    }
}
