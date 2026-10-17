package com.assessment.employees.infrastructure.rest.error;

import jakarta.annotation.Priority;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Mapea las violaciones de Bean Validation al problem+json del proyecto.
 * Reemplaza al mapper integrado de RESTEasy Reactive (que responde en
 * application/json) para conservar un único formato de error.
 */
@Provider
@Priority(1)
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {
    /** Inyección por campo: es la única vía que JAX-RS ofrece para UriInfo en un ExceptionMapper. */
    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        Problem problem = GlobalExceptionHandler.toValidationProblem(exception);
        return GlobalExceptionHandler.problemResponse(problem, uriInfo);
    }
}
