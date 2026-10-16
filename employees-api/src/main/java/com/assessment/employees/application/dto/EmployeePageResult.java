package com.assessment.employees.application.dto;

import java.util.List;

public record EmployeePageResult(
    List<EmployeeResult> items,
    int page,
    int size,
    long totalItems
) {
    public EmployeePageResult {
        items = List.copyOf(items);
    }
}