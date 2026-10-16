package com.assessment.employees.infrastructure.rest.mapper;

import com.assessment.employees.application.dto.CreateEmployeeCommand;
import com.assessment.employees.application.dto.EmployeeResult;
import com.assessment.employees.application.dto.UpdateEmployeeCommand;
import com.assessment.employees.domain.model.Document;
import com.assessment.employees.infrastructure.rest.dto.CreateEmployeeRequest;
import com.assessment.employees.infrastructure.rest.dto.EmployeeResponse;
import com.assessment.employees.infrastructure.rest.dto.UpdateEmployeeRequest;

/**
 * Mapper between REST DTOs and application DTOs.
 */
public final class EmployeeMapper {
    
    private EmployeeMapper() {}
    
    /** Convierte el par de strings de la URL en la identidad validada del dominio. */
    public static Document toDocument(String documentType, String documentNumber) {
        return Document.of(documentType, documentNumber);
    }
    
    public static CreateEmployeeCommand toCommand(CreateEmployeeRequest request) {
        return new CreateEmployeeCommand(
            Document.of(request.documentType(), request.documentNumber()),
            request.firstName(),
            request.lastName(),
            request.email(),
            request.position(),
            request.hireDate()
        );
    }
    
    public static UpdateEmployeeCommand toCommand(String documentType, String documentNumber,
                                                   UpdateEmployeeRequest request) {
        return new UpdateEmployeeCommand(
            Document.of(documentType, documentNumber),
            request.firstName(),
            request.lastName(),
            request.email(),
            request.position(),
            request.hireDate()
        );
    }
    
    public static EmployeeResponse toResponse(EmployeeResult result) {
        return new EmployeeResponse(
            result.documentType(),
            result.documentNumber(),
            result.firstName(),
            result.lastName(),
            result.email(),
            result.position(),
            result.hireDate()
        );
    }
}