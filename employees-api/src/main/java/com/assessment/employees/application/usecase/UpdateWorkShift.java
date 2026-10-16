package com.assessment.employees.application.usecase;

import com.assessment.employees.application.dto.UpdateWorkShiftCommand;
import com.assessment.employees.application.dto.WorkShiftResult;
import com.assessment.employees.domain.exception.EmployeeNotFoundException;
import com.assessment.employees.domain.exception.WorkShiftNotFoundException;
import com.assessment.employees.domain.exception.WorkShiftOverlapException;
import com.assessment.employees.domain.model.EmployeeId;
import com.assessment.employees.domain.model.WorkShift;
import com.assessment.employees.domain.repository.EmployeeRepository;
import com.assessment.employees.domain.repository.WorkShiftRepository;

import java.util.UUID;

/**
 * Use case for updating a work shift.
 */
public class UpdateWorkShift {
    private final WorkShiftRepository workShiftRepository;
    private final EmployeeRepository employeeRepository;
    
    public UpdateWorkShift(WorkShiftRepository workShiftRepository,
                           EmployeeRepository employeeRepository) {
        this.workShiftRepository = workShiftRepository;
        this.employeeRepository = employeeRepository;
    }
    
    public WorkShiftResult execute(UpdateWorkShiftCommand command) {
        java.util.UUID id = command.id();
        EmployeeId employeeId = new EmployeeId(UUID.fromString(command.employeeId()));
        
        // Check if work shift exists
        WorkShift existing = workShiftRepository.findById(id)
            .orElseThrow(() -> new WorkShiftNotFoundException(id));
        
        // Verify the work shift belongs to the specified employee
        if (!existing.getEmployeeId().value().equals(employeeId.value())) {
            throw new WorkShiftNotFoundException(id);
        }
        
        // Check if employee exists
        if (!employeeRepository.existsById(employeeId)) {
            throw new EmployeeNotFoundException(employeeId);
        }
        
        // Check for overlapping shifts (excluding current shift)
        var overlapping = workShiftRepository.findOverlapping(
            employeeId,
            command.date(),
            command.startTime(),
            command.endTime()
        ).stream()
         .filter(ws -> !ws.getId().equals(id))
         .toList();
        
        if (!overlapping.isEmpty()) {
            throw new WorkShiftOverlapException(
                command.date(),
                command.startTime(),
                command.endTime()
            );
        }
        
        // Update work shift
        existing.update(
            command.date(),
            command.startTime(),
            command.endTime()
        );
        
        // Save to repository
        WorkShift updated = workShiftRepository.save(existing);
        
        return WorkShiftResult.fromDomain(updated);
    }
}