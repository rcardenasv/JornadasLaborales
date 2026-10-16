package com.assessment.employees.infrastructure.persistence.mapper;

import com.assessment.employees.domain.model.Employee;
import com.assessment.employees.domain.model.EmployeeId;
import com.assessment.employees.domain.model.WorkShift;
import com.assessment.employees.infrastructure.persistence.entity.EmployeeEntity;
import com.assessment.employees.infrastructure.persistence.entity.WorkShiftEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PersistenceMapperTest {
    @Test
    void mapsEmployeeToEntityAndBack() {
        Employee employee = Employee.create("EMP001", "Ana", "Diaz", "ana@example.com",
            "Analyst", LocalDate.now());
        EmployeeEntity entity = EmployeeMapper.toEntity(employee);
        Employee restored = EmployeeMapper.toDomain(entity);
        assertEquals(employee.getId().value(), entity.getId());
        assertEquals(employee.getId(), restored.getId());
        assertEquals(employee.getEmail(), restored.getEmail());
        assertEquals(employee.getDocumentNumber(), restored.getDocumentNumber());
    }

    @Test
    void mapsWorkShiftToEntityAndBack() {
        EmployeeId employeeId = EmployeeId.random();
        WorkShift shift = WorkShift.create(employeeId, LocalDate.now(),
            LocalTime.of(9, 0), LocalTime.of(17, 0));
        WorkShiftEntity entity = WorkShiftMapper.toEntity(shift);
        WorkShift restored = WorkShiftMapper.toDomain(entity);
        assertEquals(shift.getId(), entity.getId());
        assertEquals(shift.getId(), restored.getId());
        assertEquals(employeeId, restored.getEmployeeId());
        assertEquals(shift.getWorkedHours(), restored.getWorkedHours());
    }

    @Test
    void restoresShiftWithExplicitUuid() {
        UUID shiftId = UUID.randomUUID();
        UUID employeeId = UUID.randomUUID();
        WorkShiftEntity entity = new WorkShiftEntity(shiftId, employeeId,
            LocalDate.now(), LocalTime.of(8, 0), LocalTime.of(16, 0));
        WorkShift restored = WorkShiftMapper.toDomain(entity);
        assertEquals(shiftId, restored.getId());
        assertEquals(employeeId, restored.getEmployeeId().value());
    }
}
