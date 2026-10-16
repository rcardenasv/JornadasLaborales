package com.assessment.employees.domain.model;

import com.assessment.employees.domain.exception.InvalidEmployeeDataException;

import java.util.Locale;

public enum DocumentType {
    CC,
    CE,
    PA;

    public static DocumentType from(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidEmployeeDataException("El tipo de documento es obligatorio.");
        }
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new InvalidEmployeeDataException("El tipo de documento debe ser CC, CE o PA.");
        }
    }
}
