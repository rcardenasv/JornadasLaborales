package com.assessment.employees.application.usecase;

import com.assessment.employees.application.dto.WorkShiftResult;
import com.assessment.employees.domain.exception.EmployeeNotFoundException;
import com.assessment.employees.domain.model.EmployeeId;
import com.assessment.employees.domain.repository.EmployeeRepository;
import com.assessment.employees.domain.repository.WorkShiftRepository;

import java.time.LocalDate;
import java.util.List;

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
    
    public List<WorkShiftResult> execute(String employeeId, LocalDate from, LocalDate to) {
        EmployeeId empId = new EmployeeId(java.util.UUID.fromString(employeeId));
        
        // Check if employee exists
        if (!employeeRepository.existsById(empId)) {
            throw new EmployeeNotFoundException(empId);
        }
        
        // Validate date range
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("La fecha desde no puede ser posterior a la fecha hasta.");
        }
        
        List<com.assessment.employees.domain.model.WorkShift> shifts = from == null && to == null
            ? workShiftRepository.findByEmployeeId(empId)
            : workShiftRepository.findByEmployeeIdAndDateRange(empId, from, to);

        return shifts.stream().map(WorkShiftResult::fromDomain).toList();
    }
}