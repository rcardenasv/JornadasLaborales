package com.assessment.employees.domain.model;

import com.assessment.employees.domain.exception.InvalidEmployeeDataException;

public record Document(DocumentType type, String number) {
    public Document {
        if (type == null) {
            throw new InvalidEmployeeDataException("El tipo de documento es obligatorio.");
        }
        if (number == null || number.isBlank()) {
            throw new InvalidEmployeeDataException("El número de documento es obligatorio.");
        }
        boolean validNumber = switch (type) {
            case CC, CE -> number.length() >= 3 && number.length() <= 20
                && number.matches("\\d+");
            case PA -> number.length() >= 5 && number.length() <= 20
                && number.matches("[a-zA-Z0-9]+");
        };
        if (!validNumber) {
            throw new InvalidEmployeeDataException("El número de documento no tiene un formato válido.");
        }
    }

    public static Document of(String type, String number) {
        return new Document(DocumentType.from(type), number);
    }
}
