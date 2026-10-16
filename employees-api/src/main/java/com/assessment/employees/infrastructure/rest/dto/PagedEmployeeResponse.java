package com.assessment.employees.infrastructure.rest.dto;

import java.util.List;

/**
 * Response DTO for paginated employee list.
 */
public record PagedEmployeeResponse(
    List<EmployeeResponse> items,
    int page,
    int size,
    long totalItems
) {}