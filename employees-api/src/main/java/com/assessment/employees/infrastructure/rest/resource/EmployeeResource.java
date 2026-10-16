package com.assessment.employees.infrastructure.rest.resource;

import com.assessment.employees.application.dto.EmployeePageResult;
import com.assessment.employees.application.dto.EmployeeResult;
import com.assessment.employees.application.dto.UpdateEmployeeCommand;
import com.assessment.employees.application.usecase.CreateEmployee;
import com.assessment.employees.application.usecase.DeleteEmployee;
import com.assessment.employees.application.usecase.GetEmployee;
import com.assessment.employees.application.usecase.ListEmployees;
import com.assessment.employees.application.usecase.UpdateEmployee;
import com.assessment.employees.infrastructure.rest.dto.CreateEmployeeRequest;
import com.assessment.employees.infrastructure.rest.dto.EmployeeResponse;
import com.assessment.employees.infrastructure.rest.dto.PagedEmployeeResponse;
import com.assessment.employees.infrastructure.rest.dto.UpdateEmployeeRequest;
import com.assessment.employees.infrastructure.rest.mapper.EmployeeMapper;
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

import java.util.List;

/**
 * REST resource for Employee operations.
 */
@Path("/api/v1/employees")
@Produces("application/json")
@Consumes("application/json")
public class EmployeeResource {
    
    private final CreateEmployee createEmployee;
    private final GetEmployee getEmployee;
    private final ListEmployees listEmployees;
    private final UpdateEmployee updateEmployee;
    private final DeleteEmployee deleteEmployee;
    
    @Inject
    public EmployeeResource(CreateEmployee createEmployee,
                           GetEmployee getEmployee,
                           ListEmployees listEmployees,
                           UpdateEmployee updateEmployee,
                           DeleteEmployee deleteEmployee) {
        this.createEmployee = createEmployee;
        this.getEmployee = getEmployee;
        this.listEmployees = listEmployees;
        this.updateEmployee = updateEmployee;
        this.deleteEmployee = deleteEmployee;
    }
    
    @POST
    @Operation(summary = "Crear empleado", description = "Crea un empleado y devuelve su URI en Location.")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "Empleado creado"),
        @APIResponse(responseCode = "400", description = "Datos inválidos"),
        @APIResponse(responseCode = "409", description = "Documento o correo duplicado")
    })
    public Response create(@Valid CreateEmployeeRequest request, @Context UriInfo uriInfo) {
        EmployeeResult result = createEmployee.execute(EmployeeMapper.toCommand(request));
        return Response.created(uriInfo.getAbsolutePathBuilder().path(result.id().toString()).build())
            .entity(EmployeeMapper.toResponse(result)).build();
    }
    
    @GET
    @Operation(summary = "Listar empleados", description = "Lista empleados ordenados por página.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Página de empleados"),
        @APIResponse(responseCode = "400", description = "Paginación inválida")
    })
    public Response list(@Parameter(description = "Página basada en cero", example = "0")
                         @QueryParam("page") @DefaultValue("0") int page,
                         @Parameter(description = "Cantidad de elementos por página; máximo 100", example = "20")
                         @QueryParam("size") @DefaultValue("20") int size) {
        EmployeePageResult result = listEmployees.execute(page, size);
        List<EmployeeResponse> responses = result.items().stream()
            .map(EmployeeMapper::toResponse)
                .toList();
        
        PagedEmployeeResponse response = new PagedEmployeeResponse(
            responses,
            result.page(),
            result.size(),
            result.totalItems()
        );
        
        return Response.ok(response).build();
    }
    
    @GET
    @Path("/{id}")
    @Operation(summary = "Obtener empleado", description = "Busca un empleado por su UUID.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Empleado encontrado"),
        @APIResponse(responseCode = "400", description = "UUID inválido"),
        @APIResponse(responseCode = "404", description = "Empleado no encontrado")
    })
    public Response get(@Parameter(description = "UUID del empleado", example = "550e8400-e29b-41d4-a716-446655440001")
                        @PathParam("id") String id) {
        EmployeeResult result = getEmployee.execute(id);
        return Response.ok(EmployeeMapper.toResponse(result)).build();
    }
    
    @PUT
    @Path("/{id}")
    @Operation(summary = "Actualizar empleado", description = "Reemplaza todos los datos editables del empleado.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Empleado actualizado"),
        @APIResponse(responseCode = "400", description = "UUID o datos inválidos"),
        @APIResponse(responseCode = "404", description = "Empleado no encontrado"),
        @APIResponse(responseCode = "409", description = "Documento o correo duplicado")
    })
    public Response update(@Parameter(description = "UUID del empleado", example = "550e8400-e29b-41d4-a716-446655440001")
                           @PathParam("id") String id,
                           @Valid UpdateEmployeeRequest request) {
        UpdateEmployeeCommand command = EmployeeMapper.toCommand(id, request);
        EmployeeResult result = updateEmployee.execute(command);
        return Response.ok(EmployeeMapper.toResponse(result)).build();
    }
    
    @DELETE
    @Path("/{id}")
    @Operation(summary = "Eliminar empleado", description = "Elimina el empleado y sus jornadas asociadas.")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "Empleado eliminado"),
        @APIResponse(responseCode = "400", description = "UUID inválido"),
        @APIResponse(responseCode = "404", description = "Empleado no encontrado")
    })
    public Response delete(@Parameter(description = "UUID del empleado", example = "550e8400-e29b-41d4-a716-446655440001")
                           @PathParam("id") String id) {
        deleteEmployee.execute(id);
        return Response.noContent().build();
    }
}