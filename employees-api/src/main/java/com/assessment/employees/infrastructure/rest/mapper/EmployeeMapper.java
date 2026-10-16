package com.assessment.employees.infrastructure.rest.mapper;

import com.assessment.employees.application.dto.CreateEmployeeCommand;
import com.assessment.employees.application.dto.EmployeeResult;
import com.assessment.employees.application.dto.UpdateEmployeeCommand;
import com.assessment.employees.infrastructure.rest.dto.CreateEmployeeRequest;
import com.assessment.employees.infrastructure.rest.dto.EmployeeResponse;
import com.assessment.employees.infrastructure.rest.dto.UpdateEmployeeRequest;

/**
 * Mapper between REST DTOs and application DTOs.
 */
public final class EmployeeMapper {
    
    private EmployeeMapper() {}
    
    public static CreateEmployeeCommand toCommand(CreateEmployeeRequest request) {
        return new CreateEmployeeCommand(
            request.documentNumber(),
            request.firstName(),
            request.lastName(),
            request.email(),
            request.position(),
            request.hireDate()
        );
    }
    
    public static UpdateEmployeeCommand toCommand(String id, UpdateEmployeeRequest request) {
        return new UpdateEmployeeCommand(
            id,
            request.documentNumber(),
            request.firstName(),
            request.lastName(),
            request.email(),
            request.position(),
            request.hireDate()
        );
    }
    
    public static EmployeeResponse toResponse(EmployeeResult result) {
        return new EmployeeResponse(
            result.id(),
            result.documentNumber(),
            result.firstName(),
            result.lastName(),
            result.email(),
            result.position(),
            result.hireDate()
        );
    }
}