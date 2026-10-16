package com.assessment.employees.infrastructure.persistence.mapper;

import com.assessment.employees.domain.model.EmployeeId;
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
            new EmployeeId(entity.getEmployeeId()),
            entity.getDate(),
            entity.getStartTime(),
            entity.getEndTime()
        );
    }
    
    public static WorkShiftEntity toEntity(WorkShift domain) {
        return new WorkShiftEntity(
            domain.getId(),
            domain.getEmployeeId().value(),
            domain.getDate(),
            domain.getStartTime(),
            domain.getEndTime()
        );
    }
}