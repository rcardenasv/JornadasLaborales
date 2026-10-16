package com.assessment.employees.infrastructure.persistence.mapper;

import com.assessment.employees.domain.model.Document;
import com.assessment.employees.domain.model.WorkShift;
import com.assessment.employees.infrastructure.persistence.entity.WorkShiftEntity;

/**
 * Mapper between WorkShiftEntity and WorkShift domain model.
 */
public final class WorkShiftMapper {
    
    private WorkShiftMapper() {}
    
    public static WorkShift toDomain(WorkShiftEntity entity) {
        return new WorkShift(
            entity.getId(),
            Document.of(entity.getEmployeeDocumentType(), entity.getEmployeeDocumentNumber()),
            entity.getDate(),
            entity.getStartTime(),
            entity.getEndTime()
        );
    }
    
    public static WorkShiftEntity toEntity(WorkShift domain) {
        return new WorkShiftEntity(
            domain.getId(),
            domain.getEmployeeDocument().type().name(),
            domain.getEmployeeDocument().number(),
            domain.getDate(),
            domain.getStartTime(),
            domain.getEndTime()
        );
    }
}