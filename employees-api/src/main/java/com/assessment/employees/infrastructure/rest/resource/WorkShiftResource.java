package com.assessment.employees.infrastructure.rest.resource;

import com.assessment.employees.application.dto.BulkCreateWorkShiftsResult;
import com.assessment.employees.application.dto.WorkShiftResult;
import com.assessment.employees.application.usecase.BulkCreateWorkShifts;
import com.assessment.employees.application.usecase.CreateWorkShift;
import com.assessment.employees.application.usecase.DeleteWorkShift;
import com.assessment.employees.application.usecase.GetWorkShift;
import com.assessment.employees.application.usecase.ListEmployeeWorkShifts;
import com.assessment.employees.application.usecase.UpdateWorkShift;
import com.assessment.employees.infrastructure.rest.dto.BulkCreateWorkShiftsRequest;
import com.assessment.employees.infrastructure.rest.dto.BulkCreateWorkShiftsResponse;
import com.assessment.employees.infrastructure.rest.dto.CreateWorkShiftRequest;
import com.assessment.employees.infrastructure.rest.dto.UpdateWorkShiftRequest;
import com.assessment.employees.infrastructure.rest.mapper.WorkShiftMapper;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import java.util.List;

@Path("/api/v1/employees/{documentType}/{documentNumber}/work-shifts")
@Produces("application/json")
@Consumes("application/json")
public class WorkShiftResource {
    /** Subruta del endpoint de carga agrupada. */
    private static final String BULK_PATH = "/bulk";

    private final CreateWorkShift createWorkShift;
    private final BulkCreateWorkShifts bulkCreateWorkShifts;
    private final GetWorkShift getWorkShift;
    private final ListEmployeeWorkShifts listWorkShifts;
    private final UpdateWorkShift updateWorkShift;
    private final DeleteWorkShift deleteWorkShift;

    @Inject
    public WorkShiftResource(CreateWorkShift createWorkShift, BulkCreateWorkShifts bulkCreateWorkShifts,
                             GetWorkShift getWorkShift,
                             ListEmployeeWorkShifts listWorkShifts,
                             UpdateWorkShift updateWorkShift, DeleteWorkShift deleteWorkShift) {
        this.createWorkShift = createWorkShift;
        this.bulkCreateWorkShifts = bulkCreateWorkShifts;
        this.getWorkShift = getWorkShift;
        this.listWorkShifts = listWorkShifts;
        this.updateWorkShift = updateWorkShift;
        this.deleteWorkShift = deleteWorkShift;
    }

    @POST
    @Operation(summary = "Crear jornada", description = "Agrega una jornada laboral al empleado.")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "Jornada creada"),
        @APIResponse(responseCode = "400", description = "Datos inválidos"),
        @APIResponse(responseCode = "404", description = "Empleado no encontrado"),
        @APIResponse(responseCode = "409", description = "Solape o tope semanal excedido")
    })
    public Response create(@PathParam("documentType") String documentType,
                           @PathParam("documentNumber") String documentNumber,
                           @Valid CreateWorkShiftRequest request,
                           @Context UriInfo uriInfo) {
        WorkShiftResult result = createWorkShift.execute(
            WorkShiftMapper.toCommand(documentType, documentNumber, request)
        );
        return Response.created(uriInfo.getAbsolutePathBuilder().path(result.id().toString()).build())
            .entity(WorkShiftMapper.toResponse(result))
            .build();
    }

    @POST
    @Path("/bulk")
    @Operation(
        summary = "Crear jornadas en bloque",
        description = "Crea todas las jornadas del rango de forma atómica; weekdays omitido significa todos los días."
    )
    @APIResponses({
        @APIResponse(responseCode = "201", description = "Jornadas creadas"),
        @APIResponse(responseCode = "400", description = "Rango, días u horario inválido"),
        @APIResponse(responseCode = "404", description = "Empleado no encontrado"),
        @APIResponse(responseCode = "409", description = "Solape o tope semanal excedido; no se crea ninguna jornada")
    })
    public Response bulkCreate(@PathParam("documentType") String documentType,
                               @PathParam("documentNumber") String documentNumber,
                               @Valid BulkCreateWorkShiftsRequest request,
                               @Context UriInfo uriInfo) {
        BulkCreateWorkShiftsResult result = bulkCreateWorkShifts.execute(
            WorkShiftMapper.toCommand(documentType, documentNumber, request)
        );
        List<WorkShiftResult> createdShifts = result.createdShifts();
        BulkCreateWorkShiftsResponse response = new BulkCreateWorkShiftsResponse(
            createdShifts.stream().map(WorkShiftMapper::toResponse).toList(),
            createdShifts.size(),
            WorkShiftMapper.toHours(result.scheduledHours())
        );
        String listPath = uriInfo.getPath()
            .substring(0, uriInfo.getPath().lastIndexOf(BULK_PATH));
        return Response.created(uriInfo.getAbsolutePathBuilder()
                .replacePath(listPath)
                .queryParam("from", request.from())
                .queryParam("to", request.to())
                .build())
            .entity(response)
            .build();
    }

    @GET
    @Operation(summary = "Listar jornadas", description = "Lista jornadas y su carga semanal.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Jornadas del empleado"),
        @APIResponse(responseCode = "400", description = "Fecha o rango inválido"),
        @APIResponse(responseCode = "404", description = "Empleado no encontrado")
    })
    public Response list(@PathParam("documentType") String documentType,
                         @PathParam("documentNumber") String documentNumber,
                         @Parameter(description = "Fecha inicial inclusiva (yyyy-MM-dd)")
                         @QueryParam("from") String from,
                         @Parameter(description = "Fecha final inclusiva (yyyy-MM-dd)")
                         @QueryParam("to") String to) {
        return Response.ok(WorkShiftMapper.toListResponse(listWorkShifts.execute(
            WorkShiftMapper.toDocument(documentType, documentNumber),
            WorkShiftMapper.parseIsoDate(from), WorkShiftMapper.parseIsoDate(to)
        ))).build();
    }

    @GET
    @Path("/{shiftId}")
    @Operation(summary = "Obtener jornada", description = "Busca una jornada del empleado.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Jornada encontrada"),
        @APIResponse(responseCode = "400", description = "UUID inválido"),
        @APIResponse(responseCode = "404", description = "Empleado o jornada no encontrado")
    })
    public Response get(@PathParam("documentType") String documentType,
                        @PathParam("documentNumber") String documentNumber,
                        @PathParam("shiftId") String shiftId) {
        WorkShiftResult result = getWorkShift.execute(
            WorkShiftMapper.toShiftId(shiftId),
            WorkShiftMapper.toDocument(documentType, documentNumber)
        );
        return Response.ok(WorkShiftMapper.toResponse(result)).build();
    }

    @PUT
    @Path("/{shiftId}")
    @Operation(summary = "Actualizar jornada", description = "Reemplaza fecha y horario.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Jornada actualizada"),
        @APIResponse(responseCode = "400", description = "UUID o datos inválidos"),
        @APIResponse(responseCode = "404", description = "Empleado o jornada no encontrado"),
        @APIResponse(responseCode = "409", description = "Solape o tope semanal excedido")
    })
    public Response update(@PathParam("documentType") String documentType,
                           @PathParam("documentNumber") String documentNumber,
                           @PathParam("shiftId") String shiftId,
                           @Valid UpdateWorkShiftRequest request) {
        WorkShiftResult result = updateWorkShift.execute(WorkShiftMapper.toCommand(
            WorkShiftMapper.toShiftId(shiftId),
            WorkShiftMapper.toDocument(documentType, documentNumber), request
        ));
        return Response.ok(WorkShiftMapper.toResponse(result)).build();
    }

    @DELETE
    @Path("/{shiftId}")
    @Operation(summary = "Eliminar jornada", description = "Elimina la jornada.")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "Jornada eliminada"),
        @APIResponse(responseCode = "400", description = "UUID inválido"),
        @APIResponse(responseCode = "404", description = "Empleado o jornada no encontrado")
    })
    public Response delete(@PathParam("documentType") String documentType,
                           @PathParam("documentNumber") String documentNumber,
                           @PathParam("shiftId") String shiftId) {
        deleteWorkShift.execute(
            WorkShiftMapper.toShiftId(shiftId),
            WorkShiftMapper.toDocument(documentType, documentNumber)
        );
        return Response.noContent().build();
    }
}
