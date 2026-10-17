package com.assessment.employees.domain.model;

import com.assessment.employees.domain.exception.InvalidEmployeeDataException;

import java.util.regex.Pattern;

/**
 * Value object representing an Email address.
 */
public record Email(String value) {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_!#$%&'*+/=?.^`~\\-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$"
    );
    
    public Email {
        if (value == null || value.isBlank()) {
            throw new InvalidEmployeeDataException("El correo electrónico es obligatorio.");
        }
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new InvalidEmployeeDataException("El correo electrónico no tiene un formato válido.");
        }
    }
    
    public static Email of(String value) {
        return new Email(value);
    }
}