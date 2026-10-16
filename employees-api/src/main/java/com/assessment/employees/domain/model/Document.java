package com.assessment.employees.domain.model;

import com.assessment.employees.domain.exception.InvalidEmployeeDataException;

/**
 * Identidad del empleado: tipo y número de documento. Inmutable tras creación;
 * el tipo se normaliza a mayúsculas y el formato depende de cada tipo.
 */
public record Document(DocumentType type, String number) {
    /** CC y CE: solo dígitos, longitud mínima. */
    private static final int MIN_DIGIT_NUMBER_LENGTH = 3;
    /** PA: alfanumérico, longitud mínima. */
    private static final int MIN_ALPHANUMBER_LENGTH = 5;
    /** Longitud máxima común para todos los tipos. */
    private static final int MAX_NUMBER_LENGTH = 20;

    public Document {
        if (type == null) {
            throw new InvalidEmployeeDataException("El tipo de documento es obligatorio.");
        }
        if (number == null || number.isBlank()) {
            throw new InvalidEmployeeDataException("El número de documento es obligatorio.");
        }
        boolean validNumber = switch (type) {
            case CC, CE -> number.length() >= MIN_DIGIT_NUMBER_LENGTH
                && number.length() <= MAX_NUMBER_LENGTH
                && number.matches("\\d+");
            case PA -> number.length() >= MIN_ALPHANUMBER_LENGTH
                && number.length() <= MAX_NUMBER_LENGTH
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
