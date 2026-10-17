package com.assessment.employees.infrastructure.rest.error;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ValidationExceptionMapperTest {
    @Test
    void mapsConstraintViolationsToProblemJson() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path propertyPath = mock(Path.class);
        when(violation.getPropertyPath()).thenReturn(propertyPath);
        when(propertyPath.toString()).thenReturn("create.request.email");
        when(violation.getMessage()).thenReturn("El correo electrónico es obligatorio.");

        Response response = new ValidationExceptionMapper()
            .toResponse(new ConstraintViolationException(Set.of(violation)));

        assertEquals(400, response.getStatus());
        assertEquals("application/problem+json", response.getMediaType().toString());
        Problem problem = (Problem) response.getEntity();
        assertEquals("Error de validación", problem.title());
        assertEquals("La solicitud contiene datos inválidos.", problem.detail());
        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) problem.errors();
        assertTrue(errors.containsKey("create.request.email"));
    }
}
