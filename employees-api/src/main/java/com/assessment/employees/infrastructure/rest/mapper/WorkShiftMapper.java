package com.assessment.employees.infrastructure.rest.mapper;

import com.assessment.employees.application.dto.CreateWorkShiftCommand;
import com.assessment.employees.application.dto.UpdateWorkShiftCommand;
import com.assessment.employees.application.dto.WorkShiftResult;
import com.assessment.employees.infrastructure.rest.dto.CreateWorkShiftRequest;
import com.assessment.employees.infrastructure.rest.dto.UpdateWorkShiftRequest;
import com.assessment.employees.infrastructure.rest.dto.WorkShiftResponse;

import java.util.UUID;

/**
 * Mapper between REST DTOs and application DTOs.
 */
public final class WorkShiftMapper {
    
    private WorkShiftMapper() {}
    
    public static CreateWorkShiftCommand toCommand(String employeeId, CreateWorkShiftRequest request) {
        return new CreateWorkShiftCommand(
            employeeId,
            request.date(),
            request.startTime(),
            request.endTime()
        );
    }
    
    public static UpdateWorkShiftCommand toCommand(UUID shiftId, String employeeId,
                                                   UpdateWorkShiftRequest request) {
        return new UpdateWorkShiftCommand(
            shiftId,
            employeeId,
            request.date(),
            request.startTime(),
            request.endTime()
        );
    }
    
    public static WorkShiftResponse toResponse(WorkShiftResult result) {
        return new WorkShiftResponse(
            result.id(),
            result.employeeId(),
            result.date(),
            result.startTime(),
            result.endTime(),
            result.workedHours()
        );
    }
}