package com.assessment.employees.infrastructure.rest.error;

import com.assessment.employees.domain.exception.DuplicateEmployeeException;
import com.assessment.employees.domain.exception.EmployeeNotFoundException;
import com.assessment.employees.domain.exception.InvalidDateRangeException;
import com.assessment.employees.domain.exception.InvalidEmployeeDataException;
import com.assessment.employees.domain.exception.InvalidPaginationException;
import com.assessment.employees.domain.exception.InvalidWorkShiftException;
import com.assessment.employees.domain.exception.WeeklyHoursExceededException;
import com.assessment.employees.domain.exception.WorkShiftBulkConflictException;
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
 * Agrupa las respuestas por categoría (404, 409, 400 de dominio, validación,
 * plataforma e inesperado) manteniendo la precisión de cada mensaje.
 */
@Provider
public class GlobalExceptionHandler implements ExceptionMapper<Throwable> {
    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionHandler.class);

    /** Inyección por campo: es la única vía que JAX-RS ofrece para UriInfo en un ExceptionMapper. */
    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(Throwable exception) {
        return respond(toProblem(exception));
    }

    private Problem toProblem(Throwable exception) {
        Problem mapped = domainProblem(exception);
        if (mapped == null) {
            mapped = validationProblem(exception);
        }
        if (mapped == null) {
            mapped = platformProblem(exception);
        }
        return mapped != null ? mapped : unexpectedProblem(exception);
    }

    private Problem domainProblem(Throwable exception) {
        Problem problem = notFoundProblem(exception);
        if (problem == null) {
            problem = conflictProblem(exception);
        }
        return problem != null ? problem : badRequestProblem(exception);
    }

    private Problem notFoundProblem(Throwable exception) {
        if (exception instanceof EmployeeNotFoundException) {
            return Problem.notFound("No se encontró el empleado solicitado.");
        }
        if (exception instanceof WorkShiftNotFoundException) {
            return Problem.notFound("No se encontró la jornada laboral solicitada.");
        }
        return null;
    }

    private Problem conflictProblem(Throwable exception) {
        if (exception instanceof DuplicateEmployeeException
                || exception instanceof WorkShiftOverlapException
                || exception instanceof WeeklyHoursExceededException
                || exception instanceof WorkShiftBulkConflictException) {
            return Problem.conflict(exception.getMessage());
        }
        return null;
    }

    private Problem badRequestProblem(Throwable exception) {
        if (exception instanceof InvalidEmployeeDataException
                || exception instanceof InvalidWorkShiftException
                || exception instanceof InvalidDateRangeException
                || exception instanceof InvalidPaginationException) {
            return Problem.badRequest(exception.getMessage());
        }
        return null;
    }

    private Problem validationProblem(Throwable exception) {
        if (!(exception instanceof ConstraintViolationException violationException)) {
            return null;
        }
        return toValidationProblem(violationException);
    }

    static Problem toValidationProblem(ConstraintViolationException exception) {
        Map<String, String> errors = new HashMap<>();
        for (ConstraintViolation<?> violation : exception.getConstraintViolations()) {
            String path = violation.getPropertyPath().toString();
            errors.put(path, violation.getMessage());
        }
        return Problem.validationError("La solicitud contiene datos inválidos.", errors);
    }

    private Problem platformProblem(Throwable exception) {
        if (exception instanceof IllegalArgumentException || exception instanceof DateTimeException
                || exception instanceof BadRequestException) {
            return Problem.badRequest(exception.getMessage() == null
                ? "La solicitud contiene datos inválidos." : exception.getMessage());
        }
        if (exception instanceof WebApplicationException webException) {
            return webApplicationProblem(webException);
        }
        return null;
    }

    private Problem webApplicationProblem(WebApplicationException exception) {
        int status = exception.getResponse().getStatus();
        if (status >= 500) {
            LOGGER.errorf("Unexpected REST exception: %s", exception.getClass().getName());
            return Problem.internalError("Ocurrió un error inesperado.");
        }
        return status == 404
            ? Problem.notFound("No se encontró el recurso solicitado.")
            : Problem.badRequest("La solicitud no pudo ser procesada.");
    }

    private Problem unexpectedProblem(Throwable exception) {
        LOGGER.errorf("Unexpected REST exception: %s", exception.getClass().getName());
        return Problem.internalError("Ocurrió un error inesperado.");
    }

    private Response respond(Problem problem) {
        return problemResponse(problem, uriInfo);
    }

    /** Unifica la serialización problem+json para todos los ExceptionMappers del proyecto. */
    static Response problemResponse(Problem problem, UriInfo uriInfo) {
        String path = uriInfo == null ? null : uriInfo.getRequestUri().getPath();
        return Response.status(problem.status())
            .type("application/problem+json")
            .entity(problem.withInstance(path))
            .build();
    }
}
