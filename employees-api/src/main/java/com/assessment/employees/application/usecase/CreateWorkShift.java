package com.assessment.employees.application.usecase;

import com.assessment.employees.application.dto.CreateWorkShiftCommand;
import com.assessment.employees.application.dto.WorkShiftResult;
import com.assessment.employees.domain.exception.EmployeeNotFoundException;
import com.assessment.employees.domain.exception.WorkShiftOverlapException;
import com.assessment.employees.domain.model.EmployeeId;
import com.assessment.employees.domain.model.WorkShift;
import com.assessment.employees.domain.repository.EmployeeRepository;
import com.assessment.employees.domain.repository.WorkShiftRepository;

import java.util.UUID;

/**
 * Use case for creating a work shift.
 */
public class CreateWorkShift {
    private final WorkShiftRepository workShiftRepository;
    private final EmployeeRepository employeeRepository;
    
    public CreateWorkShift(WorkShiftRepository workShiftRepository, 
                          EmployeeRepository employeeRepository) {
        this.workShiftRepository = workShiftRepository;
        this.employeeRepository = employeeRepository;
    }
    
    public WorkShiftResult execute(CreateWorkShiftCommand command) {
        // Check if employee exists
        EmployeeId employeeId = new EmployeeId(UUID.fromString(command.employeeId()));
        if (!employeeRepository.existsById(employeeId)) {
            throw new EmployeeNotFoundException(employeeId);
        }
        
        // Check for overlapping shifts
        var overlapping = workShiftRepository.findOverlapping(
            employeeId,
            command.date(),
            command.startTime(),
            command.endTime()
        );
        
        if (!overlapping.isEmpty()) {
            throw new WorkShiftOverlapException(
                command.date(),
                command.startTime(),
                command.endTime()
            );
        }
        
        // Create work shift
        WorkShift workShift = WorkShift.create(
            employeeId,
            command.date(),
            command.startTime(),
            command.endTime()
        );
        
        // Save to repository
        WorkShift saved = workShiftRepository.save(workShift);
        
        return WorkShiftResult.fromDomain(saved);
    }
}