package com.assessment.employees.infrastructure.persistence.mapper;

import com.assessment.employees.domain.model.Employee;
import com.assessment.employees.domain.model.EmployeeId;
import com.assessment.employees.domain.model.Email;
import com.assessment.employees.domain.model.DocumentNumber;
import com.assessment.employees.infrastructure.persistence.entity.EmployeeEntity;

/**
 * Mapper between EmployeeEntity and Employee domain model.
 */
public final class EmployeeMapper {
    
    private EmployeeMapper() {}
    
    public static Employee toDomain(EmployeeEntity entity) {
        return new Employee(
            new EmployeeId(entity.getId()),
            new DocumentNumber(entity.getDocumentNumber()),
            entity.getFirstName(),
            entity.getLastName(),
            new Email(entity.getEmail()),
            entity.getPosition(),
            entity.getHireDate()
        );
    }
    
    public static EmployeeEntity toEntity(Employee domain) {
        return new EmployeeEntity(
            domain.getId().value(),
            domain.getDocumentNumber().value(),
            domain.getFirstName(),
            domain.getLastName(),
            domain.getEmail().value(),
            domain.getPosition(),
            domain.getHireDate()
        );
    }
}