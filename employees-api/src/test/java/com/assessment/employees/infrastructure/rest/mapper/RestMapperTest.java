package com.assessment.employees.infrastructure.rest.mapper;

import com.assessment.employees.application.dto.EmployeeResult;
import com.assessment.employees.application.dto.WorkShiftResult;
import com.assessment.employees.domain.model.DayType;
import com.assessment.employees.domain.model.ShiftType;
import com.assessment.employees.infrastructure.rest.dto.CreateEmployeeRequest;
import com.assessment.employees.infrastructure.rest.dto.CreateWorkShiftRequest;
import com.assessment.employees.infrastructure.rest.dto.UpdateEmployeeRequest;
import com.assessment.employees.infrastructure.rest.dto.UpdateWorkShiftRequest;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RestMapperTest {
    @Test
    void mapsEmployeeCreateUpdateAndResponse() {
        LocalDate hireDate = LocalDate.of(2026, 1, 15);
        CreateEmployeeRequest create = new CreateEmployeeRequest("CC", "123456", "Ana", "Diaz",
            "ana@example.com", "Analyst", hireDate);
        UpdateEmployeeRequest update = new UpdateEmployeeRequest("Bea", "Lopez",
            "bea@example.com", "Lead", hireDate);
        EmployeeResult result = new EmployeeResult("CC", "123456", "Ana", "Diaz",
            "ana@example.com", "Analyst", hireDate);

        assertEquals("CC", EmployeeMapper.toCommand(create).documentType());
        assertEquals("123456", EmployeeMapper.toCommand(create).documentNumber());
        assertEquals("123456", EmployeeMapper.toCommand("CC", "123456", update).documentNumber());
        assertEquals("CC", EmployeeMapper.toResponse(result).documentType());
    }

    @Test
    void mapsWorkShiftCreateUpdateAndResponse() {
        String documentType = "CE";
        String documentNumber = "123456";
        UUID id = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 10, 15);
        LocalTime start = LocalTime.of(9, 0);
        LocalTime end = LocalTime.of(17, 0);
        CreateWorkShiftRequest create = new CreateWorkShiftRequest(date, start, end);
        UpdateWorkShiftRequest update = new UpdateWorkShiftRequest(date, start, end);
        WorkShiftResult result = new WorkShiftResult(id, documentType, documentNumber, date, start, end,
            Duration.ofHours(8), Duration.ofHours(8), Duration.ZERO, DayType.ORDINARIO, ShiftType.DIURNA);

        assertEquals(documentType, WorkShiftMapper.toCommand(documentType, documentNumber, create).documentType());
        assertEquals(documentNumber, WorkShiftMapper.toCommand(documentType, documentNumber, create).documentNumber());
        assertEquals(id, WorkShiftMapper.toCommand(id, documentType, documentNumber, update).id());
        assertEquals(id, WorkShiftMapper.toResponse(result).id());
        assertEquals(ShiftType.DIURNA, WorkShiftMapper.toResponse(result).shiftType());
    }
}
