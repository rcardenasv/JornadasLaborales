package com.assessment.employees.domain.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class WorkShiftTest {
    @Test
    void shouldCreateWorkShift() {
        EmployeeId empId = new EmployeeId(UUID.randomUUID());
        WorkShift ws = WorkShift.create(empId, LocalDate.now(), LocalTime.of(9,0), LocalTime.of(17,0));
        assertTrue(ws.getStartTime().isBefore(ws.getEndTime()));
    }
}