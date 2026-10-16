package com.assessment.employees.infrastructure.rest.mapper;

import com.assessment.employees.application.dto.CreateWorkShiftCommand;
import com.assessment.employees.application.dto.BulkCreateWorkShiftsCommand;
import com.assessment.employees.application.dto.UpdateWorkShiftCommand;
import com.assessment.employees.application.dto.WorkShiftResult;
import com.assessment.employees.infrastructure.rest.dto.CreateWorkShiftRequest;
import com.assessment.employees.infrastructure.rest.dto.BulkCreateWorkShiftsRequest;
import com.assessment.employees.infrastructure.rest.dto.UpdateWorkShiftRequest;
import com.assessment.employees.infrastructure.rest.dto.WorkShiftResponse;

import java.util.UUID;

/**
 * Mapper between REST DTOs and application DTOs.
 */
public final class WorkShiftMapper {
    
    private WorkShiftMapper() {}
    
    public static CreateWorkShiftCommand toCommand(String documentType, String documentNumber,
                                                    CreateWorkShiftRequest request) {
        return new CreateWorkShiftCommand(
            documentType,
            documentNumber,
            request.date(),
            request.startTime(),
            request.endTime()
        );
    }

    public static BulkCreateWorkShiftsCommand toCommand(String documentType, String documentNumber,
                                                         BulkCreateWorkShiftsRequest request) {
        return new BulkCreateWorkShiftsCommand(
            documentType,
            documentNumber,
            request.from(),
            request.to(),
            request.weekdays(),
            request.startTime(),
            request.endTime()
        );
    }
    
    public static UpdateWorkShiftCommand toCommand(UUID shiftId, String documentType,
                                                   String documentNumber,
                                                   UpdateWorkShiftRequest request) {
        return new UpdateWorkShiftCommand(
            shiftId,
            documentType,
            documentNumber,
            request.date(),
            request.startTime(),
            request.endTime()
        );
    }
    
    public static WorkShiftResponse toResponse(WorkShiftResult result) {
        return new WorkShiftResponse(
            result.id(),
            result.documentType(),
            result.documentNumber(),
            result.date(),
            result.startTime(),
            result.endTime(),
            result.workedHours(),
            result.dayType(),
            result.shiftType(),
            result.daytimeHours(),
            result.nighttimeHours()
        );
    }
}