package com.assessment.employees.infrastructure.rest.resource;

import com.assessment.employees.application.dto.WorkShiftResult;
import com.assessment.employees.application.dto.UpdateWorkShiftCommand;
import com.assessment.employees.application.usecase.CreateWorkShift;
import com.assessment.employees.application.usecase.DeleteWorkShift;
import com.assessment.employees.application.usecase.GetWorkShift;
import com.assessment.employees.application.usecase.ListEmployeeWorkShifts;
import com.assessment.employees.application.usecase.UpdateWorkShift;
import com.assessment.employees.infrastructure.rest.dto.CreateWorkShiftRequest;
import com.assessment.employees.infrastructure.rest.dto.WorkShiftListResponse;
import com.assessment.employees.infrastructure.rest.dto.WorkShiftResponse;
import com.assessment.employees.infrastructure.rest.dto.UpdateWorkShiftRequest;
import com.assessment.employees.infrastructure.rest.mapper.WorkShiftMapper;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * REST resource for Work Shift operations.
 */
@Path("/api/v1/employees/{employeeId}/work-shifts")
@Produces("application/json")
@Consumes("application/json")
public class WorkShiftResource {
    
    private final CreateWorkShift createWorkShift;
    private final GetWorkShift getWorkShift;
    private final ListEmployeeWorkShifts listWorkShifts;
    private final UpdateWorkShift updateWorkShift;
    private final DeleteWorkShift deleteWorkShift;
    
    @Inject
    public WorkShiftResource(CreateWorkShift createWorkShift,
                              GetWorkShift getWorkShift,
                              ListEmployeeWorkShifts listWorkShifts,
                              UpdateWorkShift updateWorkShift,
                              DeleteWorkShift deleteWorkShift) {
        this.createWorkShift = createWorkShift;
        this.getWorkShift = getWorkShift;
        this.listWorkShifts = listWorkShifts;
        this.updateWorkShift = updateWorkShift;
        this.deleteWorkShift = deleteWorkShift;
    }
    
    @POST
    @Operation(summary = "Crear jornada", description = "Agrega una jornada laboral no solapada al empleado.")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "Jornada creada"),
        @APIResponse(responseCode = "400", description = "Datos inválidos"),
        @APIResponse(responseCode = "404", description = "Empleado no encontrado"),
        @APIResponse(responseCode = "409", description = "La jornada se solapa con otra")
    })
    public Response create(@Parameter(description = "UUID del empleado", example = "550e8400-e29b-41d4-a716-446655440001")
                           @PathParam("employeeId") String employeeId,
                           @Valid CreateWorkShiftRequest request,
                           @Context UriInfo uriInfo) {
        WorkShiftResult result = createWorkShift.execute(WorkShiftMapper.toCommand(employeeId, request));
        return Response.created(uriInfo.getAbsolutePathBuilder().path(result.id().toString()).build())
            .entity(WorkShiftMapper.toResponse(result)).build();
    }
    
    @GET
    @Operation(summary = "Listar jornadas", description = "Lista las jornadas ordenadas y suma sus horas trabajadas.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Jornadas del empleado"),
        @APIResponse(responseCode = "400", description = "Fecha o rango inválido"),
        @APIResponse(responseCode = "404", description = "Empleado no encontrado")
    })
    public Response list(@Parameter(description = "UUID del empleado", example = "550e8400-e29b-41d4-a716-446655440001")
                         @PathParam("employeeId") String employeeId,
                         @Parameter(description = "Fecha inicial inclusiva (yyyy-MM-dd)", example = "2026-10-01")
                         @QueryParam("from") String from,
                         @Parameter(description = "Fecha final inclusiva (yyyy-MM-dd)", example = "2026-10-31")
                         @QueryParam("to") String to) {
        LocalDate fromDate = parseDate(from);
        LocalDate toDate = parseDate(to);
        
        List<WorkShiftResult> results = listWorkShifts.execute(employeeId, fromDate, toDate);
        List<WorkShiftResponse> responses = results.stream()
            .map(WorkShiftMapper::toResponse)
                .toList();
        
        java.time.Duration totalHours = results.stream()
                .map(WorkShiftResult::workedHours)
                .reduce(java.time.Duration.ZERO, java.time.Duration::plus);
        
        return Response.ok(new WorkShiftListResponse(responses, totalHours)).build();
    }

    private LocalDate parseDate(String value) {
        if (value == null) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Las fechas deben usar el formato ISO yyyy-MM-dd.", exception);
        }
    }
    
    @GET
    @Path("/{shiftId}")
    @Operation(summary = "Obtener jornada", description = "Busca una jornada dentro del empleado indicado.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Jornada encontrada"),
        @APIResponse(responseCode = "400", description = "UUID inválido"),
        @APIResponse(responseCode = "404", description = "Empleado o jornada no encontrado")
    })
    public Response get(@Parameter(description = "UUID del empleado", example = "550e8400-e29b-41d4-a716-446655440001")
                        @PathParam("employeeId") String employeeId,
                        @Parameter(description = "UUID de la jornada", example = "650e8400-e29b-41d4-a716-446655440001")
                        @PathParam("shiftId") String shiftId) {
        WorkShiftResult result = getWorkShift.execute(shiftId, employeeId);
        return Response.ok(WorkShiftMapper.toResponse(result)).build();
    }
    
    @PUT
    @Path("/{shiftId}")
    @Operation(summary = "Actualizar jornada", description = "Reemplaza la fecha y el horario de una jornada.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Jornada actualizada"),
        @APIResponse(responseCode = "400", description = "UUID o datos inválidos"),
        @APIResponse(responseCode = "404", description = "Empleado o jornada no encontrado"),
        @APIResponse(responseCode = "409", description = "La jornada se solapa con otra")
    })
    public Response update(@Parameter(description = "UUID del empleado", example = "550e8400-e29b-41d4-a716-446655440001")
                           @PathParam("employeeId") String employeeId,
                           @Parameter(description = "UUID de la jornada", example = "650e8400-e29b-41d4-a716-446655440001")
                           @PathParam("shiftId") String shiftId,
                           @Valid UpdateWorkShiftRequest request) {
        UpdateWorkShiftCommand command = WorkShiftMapper.toCommand(
            java.util.UUID.fromString(shiftId), employeeId, request);
        WorkShiftResult result = updateWorkShift.execute(command);
        return Response.ok(WorkShiftMapper.toResponse(result)).build();
    }
    
    @DELETE
    @Path("/{shiftId}")
    @Operation(summary = "Eliminar jornada", description = "Elimina la jornada indicada del empleado.")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "Jornada eliminada"),
        @APIResponse(responseCode = "400", description = "UUID inválido"),
        @APIResponse(responseCode = "404", description = "Empleado o jornada no encontrado")
    })
    public Response delete(@Parameter(description = "UUID del empleado", example = "550e8400-e29b-41d4-a716-446655440001")
                           @PathParam("employeeId") String employeeId,
                           @Parameter(description = "UUID de la jornada", example = "650e8400-e29b-41d4-a716-446655440001")
                           @PathParam("shiftId") String shiftId) {
        deleteWorkShift.execute(shiftId, employeeId);
        return Response.noContent().build();
    }
}