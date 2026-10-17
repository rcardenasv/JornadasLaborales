package com.assessment.employees.infrastructure.rest.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

/**
 * Response DTO for paginated employee list.
 */
@Schema(name = "PagedEmployeeResponse", description = "Página de empleados con información de paginación.")
public record PagedEmployeeResponse(
    @Schema(description = "Empleados de la página actual.")
    List<EmployeeResponse> items,

    @Schema(description = "Page basada en cero.", example = "0")
    int page,

    @Schema(description = "Cantidad solicitada de elementos; máximo 100.", example = "20")
    int size,

    @Schema(description = "Total de empleados existentes.", example = "25")
    long totalItems
) {}