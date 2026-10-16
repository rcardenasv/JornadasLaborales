package com.assessment.employees.infrastructure.rest.error;

import com.assessment.employees.domain.exception.DuplicateEmployeeException;
import com.assessment.employees.domain.exception.EmployeeNotFoundException;
import com.assessment.employees.domain.exception.WorkShiftNotFoundException;
import com.assessment.employees.domain.exception.WorkShiftOverlapException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.time.DateTimeException;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for REST API.
 */
@Provider
public class GlobalExceptionHandler implements ExceptionMapper<Throwable> {
    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionHandler.class);

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(Throwable exception) {
        if (exception instanceof EmployeeNotFoundException) {
            return respond(Problem.notFound("No se encontró el empleado solicitado."));
        }
        if (exception instanceof WorkShiftNotFoundException) {
            return respond(Problem.notFound("No se encontró la jornada laboral solicitada."));
        }
        if (exception instanceof DuplicateEmployeeException) {
            return respond(Problem.conflict("Ya existe un empleado con ese correo o número de documento."));
        }
        if (exception instanceof WorkShiftOverlapException) {
            return respond(Problem.conflict("La jornada se solapa con otra jornada del empleado."));
        }
        if (exception instanceof ConstraintViolationException) {
            Map<String, String> errors = new HashMap<>();
            for (ConstraintViolation<?> violation : ((ConstraintViolationException) exception).getConstraintViolations()) {
                String path = violation.getPropertyPath().toString();
                errors.put(path, violation.getMessage());
            }
            return respond(Problem.validationError("La solicitud contiene datos inválidos.", errors));
        }
        if (exception instanceof IllegalArgumentException || exception instanceof DateTimeException
                || exception instanceof BadRequestException) {
            return respond(Problem.badRequest(exception.getMessage() == null
                ? "La solicitud contiene datos inválidos." : exception.getMessage()));
        }
        if (exception instanceof WebApplicationException webException) {
            int status = webException.getResponse().getStatus();
            if (status >= 500) {
                LOGGER.errorf("Unexpected REST exception: %s", exception.getClass().getName());
                return respond(Problem.internalError("Ocurrió un error inesperado."));
            }
            Problem problem = status == 404
                ? Problem.notFound("No se encontró el recurso solicitado.")
                : Problem.badRequest("La solicitud no pudo ser procesada.");
            return respond(problem);
        }

        LOGGER.errorf("Unexpected REST exception: %s", exception.getClass().getName());
        return respond(Problem.internalError("Ocurrió un error inesperado."));
    }

    private Response respond(Problem problem) {
        String path = uriInfo == null ? null : uriInfo.getRequestUri().getPath();
        return Response.status(problem.status())
            .type("application/problem+json")
            .entity(problem.withInstance(path))
            .build();
    }
}