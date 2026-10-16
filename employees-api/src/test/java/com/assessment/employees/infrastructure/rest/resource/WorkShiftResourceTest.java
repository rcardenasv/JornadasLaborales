package com.assessment.employees.infrastructure.rest.resource;

import com.assessment.employees.application.dto.WorkShiftResult;
import com.assessment.employees.application.usecase.*;
import com.assessment.employees.infrastructure.rest.dto.CreateWorkShiftRequest;
import com.assessment.employees.infrastructure.rest.dto.UpdateWorkShiftRequest;
import com.assessment.employees.infrastructure.rest.dto.WorkShiftListResponse;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkShiftResourceTest {
    @Mock CreateWorkShift create;
    @Mock GetWorkShift get;
    @Mock ListEmployeeWorkShifts list;
    @Mock UpdateWorkShift update;
    @Mock DeleteWorkShift delete;
    @Mock UriInfo uriInfo;
    private WorkShiftResource resource;

    @BeforeEach
    void setUp() {
        resource = new WorkShiftResource(create, get, list, update, delete);
    }

    @Test
    void createReturns201AndLocation() {
        String employeeId = UUID.randomUUID().toString();
        WorkShiftResult shift = shift(employeeId);
        when(create.execute(any())).thenReturn(shift);
        when(uriInfo.getAbsolutePathBuilder()).thenReturn(UriBuilder.fromUri(
            "http://localhost/api/v1/employees/" + employeeId + "/work-shifts"));

        Response response = resource.create(employeeId, request(), uriInfo);

        assertEquals(201, response.getStatus());
        assertEquals("http://localhost/api/v1/employees/" + employeeId + "/work-shifts/" + shift.id(),
            response.getLocation().toString());
    }

    @Test
    void listParsesFiltersAndTotalsHours() {
        String employeeId = UUID.randomUUID().toString();
        when(list.execute(employeeId, LocalDate.parse("2026-10-01"), null)).thenReturn(List.of(shift(employeeId)));

        WorkShiftListResponse response = (WorkShiftListResponse) resource.list(employeeId,
            "2026-10-01", null).getEntity();

        assertEquals(1, response.shifts().size());
        assertEquals(Duration.ofHours(8), response.totalWorkedHours());
        assertThrows(IllegalArgumentException.class, () -> resource.list(employeeId, "yesterday", null));
    }

    @Test
    void getUpdateAndDeleteDelegate() {
        String employeeId = UUID.randomUUID().toString();
        WorkShiftResult shift = shift(employeeId);
        when(get.execute(shift.id().toString(), employeeId)).thenReturn(shift);
        when(update.execute(any())).thenReturn(shift);

        assertEquals(200, resource.get(employeeId, shift.id().toString()).getStatus());
        assertEquals(200, resource.update(employeeId, shift.id().toString(),
            new UpdateWorkShiftRequest(shift.date(), shift.startTime(), shift.endTime())).getStatus());
        assertEquals(204, resource.delete(employeeId, shift.id().toString()).getStatus());
        verify(delete).execute(shift.id().toString(), employeeId);
    }

    private CreateWorkShiftRequest request() {
        return new CreateWorkShiftRequest(LocalDate.parse("2026-10-15"),
            LocalTime.of(9, 0), LocalTime.of(17, 0));
    }

    private WorkShiftResult shift(String employeeId) {
        return new WorkShiftResult(UUID.randomUUID(), employeeId, LocalDate.parse("2026-10-01"),
            LocalTime.of(9, 0), LocalTime.of(17, 0), Duration.ofHours(8));
    }
}
