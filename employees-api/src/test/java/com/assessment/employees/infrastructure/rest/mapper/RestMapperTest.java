package com.assessment.employees.infrastructure.rest.mapper;

import com.assessment.employees.application.dto.EmployeeResult;
import com.assessment.employees.application.dto.WorkShiftResult;
import com.assessment.employees.infrastructure.rest.dto.*;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RestMapperTest {
    @Test
    void mapsEmployeeCreateUpdateAndResponse() {
        LocalDate date = LocalDate.now();
        CreateEmployeeRequest create = new CreateEmployeeRequest("EMP001", "Ana", "Diaz",
            "ana@example.com", "Analyst", date);
        UpdateEmployeeRequest update = new UpdateEmployeeRequest("EMP002", "Bea", "Lopez",
            "bea@example.com", "Lead", date);
        UUID id = UUID.randomUUID();
        EmployeeResult result = new EmployeeResult(id, "EMP001", "Ana", "Diaz",
            "ana@example.com", "Analyst", date);

        assertEquals("EMP001", EmployeeMapper.toCommand(create).documentNumber());
        assertEquals("path-id", EmployeeMapper.toCommand("path-id", update).id());
        assertEquals(id, EmployeeMapper.toResponse(result).id());
    }

    @Test
    void mapsWorkShiftCreateUpdateAndResponse() {
        String employeeId = UUID.randomUUID().toString();
        UUID id = UUID.randomUUID();
        LocalDate date = LocalDate.now();
        LocalTime start = LocalTime.of(9, 0);
        LocalTime end = LocalTime.of(17, 0);
        CreateWorkShiftRequest create = new CreateWorkShiftRequest(date, start, end);
        UpdateWorkShiftRequest update = new UpdateWorkShiftRequest(date, start, end);
        WorkShiftResult result = new WorkShiftResult(id, employeeId, date, start, end, Duration.ofHours(8));

        assertEquals(employeeId, WorkShiftMapper.toCommand(employeeId, create).employeeId());
        assertEquals(id, WorkShiftMapper.toCommand(id, employeeId, update).id());
        assertEquals(id, WorkShiftMapper.toResponse(result).id());
    }
}
