package com.assessment.employees.infrastructure.rest.resource;

import com.assessment.employees.application.dto.WorkShiftResult;
import com.assessment.employees.application.dto.BulkCreateWorkShiftsResult;
import com.assessment.employees.application.dto.WorkShiftListResult;
import com.assessment.employees.application.dto.WeeklyWorkloadSummaryResult;
import com.assessment.employees.application.usecase.*;
import com.assessment.employees.infrastructure.rest.dto.BulkCreateWorkShiftsRequest;
import com.assessment.employees.domain.model.Document;
import com.assessment.employees.domain.model.WorkShift;
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

import java.math.BigDecimal;
import java.time.Duration;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkShiftResourceTest {
    private static final String TYPE = "CC";
    private static final String NUMBER = "123456";

    @Mock CreateWorkShift create;
    @Mock BulkCreateWorkShifts bulkCreate;
    @Mock GetWorkShift get;
    @Mock ListEmployeeWorkShifts list;
    @Mock UpdateWorkShift update;
    @Mock DeleteWorkShift delete;
    @Mock UriInfo uriInfo;
    private WorkShiftResource resource;

    @BeforeEach
    void setUp() {
        resource = new WorkShiftResource(create, bulkCreate, get, list, update, delete);
    }

    @Test
    void createReturns201AndLocation() {
        WorkShiftResult shift = shift();
        when(create.execute(any())).thenReturn(shift);
        when(uriInfo.getAbsolutePathBuilder()).thenReturn(UriBuilder.fromUri(
            "http://localhost/api/v1/employees/" + TYPE + "/" + NUMBER + "/work-shifts"));

        Response response = resource.create(TYPE, NUMBER, request(), uriInfo);

        assertEquals(201, response.getStatus());
        assertEquals("http://localhost/api/v1/employees/" + TYPE + "/" + NUMBER
            + "/work-shifts/" + shift.id(), response.getLocation().toString());
    }

    @Test
    void bulkCreateReturns201AndFilteredListLocation() {
        WorkShiftResult result = shift();
        when(bulkCreate.execute(any())).thenReturn(
            new BulkCreateWorkShiftsResult(List.of(result), Duration.ofHours(8))
        );
        when(uriInfo.getAbsolutePathBuilder()).thenReturn(UriBuilder.fromUri(
            "http://localhost/api/v1/employees/" + TYPE + "/" + NUMBER + "/work-shifts/bulk"));
        when(uriInfo.getPath()).thenReturn("/api/v1/employees/" + TYPE + "/" + NUMBER + "/work-shifts/bulk");
        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 2);

        Response response = resource.bulkCreate(TYPE, NUMBER,
            new BulkCreateWorkShiftsRequest(from, to, Set.of(DayOfWeek.THURSDAY),
                LocalTime.of(9, 0), LocalTime.of(17, 0)), uriInfo);

        assertEquals(201, response.getStatus());
        assertTrue(response.getLocation().toString().contains("/work-shifts?from=2026-10-01&to=2026-10-02"));
    }

    @Test
    void listParsesFiltersAndReturnsTotalsAndWeeklySummary() {
        WorkShiftResult result = shift();
        when(list.execute(TYPE, NUMBER, LocalDate.parse("2026-10-01"), null)).thenReturn(
            new WorkShiftListResult(List.of(result), List.of(
                new WeeklyWorkloadSummaryResult(LocalDate.of(2026, 9, 28), Duration.ofHours(8),
                    Duration.ofHours(42), Duration.ZERO, Duration.ofHours(34))
            ))
        );

        WorkShiftListResponse response = (WorkShiftListResponse) resource.list(TYPE, NUMBER,
            "2026-10-01", null).getEntity();

        assertEquals(1, response.shifts().size());
        assertEquals(new BigDecimal("8.00"), response.totalWorkedHours());
        assertEquals("2026-W40", response.weeklySummaries().get(0).week());
        assertEquals(new BigDecimal("8.00"),
            response.weeklySummaries().get(0).scheduledHours());
        assertEquals(new BigDecimal("42.00"),
            response.weeklySummaries().get(0).ordinaryLimitHours());
        assertEquals(new BigDecimal("0.00"),
            response.weeklySummaries().get(0).overtimeHours());
        assertEquals(new BigDecimal("34.00"),
            response.weeklySummaries().get(0).remainingOrdinaryHours());
        assertEquals(new BigDecimal("8.00"), response.shifts().get(0).workedHours());
        assertEquals(new BigDecimal("0.00"), response.shifts().get(0).nighttimeHours());
        assertThrows(IllegalArgumentException.class,
            () -> resource.list(TYPE, NUMBER, "yesterday", null));
    }

    @Test
    void getUpdateAndDeleteDelegate() {
        WorkShiftResult result = shift();
        String shiftId = result.id().toString();
        when(get.execute(shiftId, TYPE, NUMBER)).thenReturn(result);
        when(update.execute(any())).thenReturn(result);

        assertEquals(200, resource.get(TYPE, NUMBER, shiftId).getStatus());
        assertEquals(200, resource.update(TYPE, NUMBER, shiftId,
            new UpdateWorkShiftRequest(result.date(), result.startTime(), result.endTime())).getStatus());
        assertEquals(204, resource.delete(TYPE, NUMBER, shiftId).getStatus());
        verify(delete).execute(shiftId, TYPE, NUMBER);
    }

    private CreateWorkShiftRequest request() {
        return new CreateWorkShiftRequest(LocalDate.parse("2026-10-15"),
            LocalTime.of(9, 0), LocalTime.of(17, 0));
    }

    private WorkShiftResult shift() {
        return WorkShiftResult.fromDomain(WorkShift.create(Document.of(TYPE, NUMBER),
            LocalDate.parse("2026-10-01"), LocalTime.of(9, 0), LocalTime.of(17, 0)));
    }
}
