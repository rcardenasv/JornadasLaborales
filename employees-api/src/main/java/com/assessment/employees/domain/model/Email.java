package com.assessment.employees.domain.model;

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
            throw new IllegalArgumentException("Email cannot be null or blank");
        }
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }
    
    public static Email of(String value) {
        return new Email(value);
    }
}