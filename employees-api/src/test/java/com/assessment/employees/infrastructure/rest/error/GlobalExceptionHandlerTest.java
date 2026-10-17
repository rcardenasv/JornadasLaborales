package com.assessment.employees.infrastructure.rest.error;

import com.assessment.employees.domain.exception.*;
import com.assessment.employees.domain.model.Document;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsDomainAndValidationErrorsToProblemJson() {
        assertProblem(handler.toResponse(new EmployeeNotFoundException(Document.of("CC", "123456"))), 404);
        assertProblem(handler.toResponse(new WorkShiftNotFoundException(UUID.randomUUID())), 404);
        Response duplicate = handler.toResponse(new DuplicateEmployeeException("correo"));
        assertProblem(duplicate, 409);
        assertEquals("Ya existe un empleado con ese correo.",
            ((Problem) duplicate.getEntity()).detail());
        assertProblem(handler.toResponse(new InvalidWorkShiftException("Jornada inválida.")), 400);
        Response overlap = handler.toResponse(new WorkShiftOverlapException(LocalDate.of(2026, 10, 15),
            LocalTime.of(9, 0), LocalTime.of(17, 0)));
        assertProblem(overlap, 409);
        assertTrue(((Problem) overlap.getEntity()).detail().contains("se solapa con otra jornada"));
        assertProblem(handler.toResponse(new WeeklyHoursExceededException(LocalDate.now(),
            java.time.Duration.ofHours(55), java.time.Duration.ofHours(54))), 409);
        assertProblem(handler.toResponse(new WorkShiftBulkConflictException(List.of(LocalDate.now()))), 409);
        assertProblem(handler.toResponse(new InvalidDateRangeException("Rango inválido.")), 400);
        Response genericBadRequest = handler.toResponse(new IllegalArgumentException("Entrada inválida."));
        assertProblem(genericBadRequest, 400);
        assertEquals("Solicitud inválida", ((Problem) genericBadRequest.getEntity()).title());
        assertEquals("Entrada inválida.", ((Problem) genericBadRequest.getEntity()).detail());
        assertProblem(handler.toResponse(new InvalidPaginationException("Paginación inválida.")), 400);
        assertProblem(handler.toResponse(new ConstraintViolationException(Set.of())), 400);
    }

    @Test
    void preservesClientHttpStatusAndHidesInternalDetails() {
        assertProblem(handler.toResponse(new WebApplicationException(Response.status(404).build())), 404);
        Response response = handler.toResponse(new IllegalStateException("database password"));
        assertProblem(response, 500);
        assertFalse(((Problem) response.getEntity()).detail().contains("database password"));
    }

    private void assertProblem(Response response, int expectedStatus) {
        assertEquals(expectedStatus, response.getStatus());
        assertEquals("application/problem+json", response.getMediaType().toString());
        Problem body = (Problem) response.getEntity();
        assertEquals(expectedStatus, body.status());
        assertNotNull(body.title());
    }
}
