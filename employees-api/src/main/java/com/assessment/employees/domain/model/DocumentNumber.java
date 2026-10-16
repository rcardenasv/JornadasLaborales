package com.assessment.employees.domain.model;

/**
 * Value object representing an Employee document number.
 */
public record DocumentNumber(String value) {
    public DocumentNumber {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Document number cannot be null or blank");
        }
        if (value.length() < 5 || value.length() > 20) {
            throw new IllegalArgumentException("Document number must be between 5 and 20 characters");
        }
        if (!value.matches("[a-zA-Z0-9]+")) {
            throw new IllegalArgumentException("Document number must be alphanumeric");
        }
    }
    
    public static DocumentNumber of(String value) {
        return new DocumentNumber(value);
    }
}