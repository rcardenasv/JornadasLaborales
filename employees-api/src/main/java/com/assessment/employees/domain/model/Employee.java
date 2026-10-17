package com.assessment.employees.domain.model;

import com.assessment.employees.domain.exception.InvalidEmployeeDataException;

import java.time.LocalDate;

/**
 * Employee aggregate root.
 * Represents a worker in the system.
 */
public class Employee {
    /** Longitud permitida para nombre, apellido y cargo. */
    private static final int MIN_TEXT_LENGTH = 1;
    private static final int MAX_TEXT_LENGTH = 100;

    private final EmployeeId id;
    private final Document document;
    private String firstName;
    private String lastName;
    private Email email;
    private String position;
    private final LocalDate hireDate;
    
    public Employee(EmployeeId id, Document document, String firstName,
                    String lastName, Email email, String position, LocalDate hireDate,
                    LocalDate currentDate) {
        this.id = id;
        this.document = document;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.position = position;
        this.hireDate = hireDate;

        validate(currentDate);
    }
    
    private void validate(LocalDate currentDate) {
        if (id == null || document == null || email == null) {
            throw new InvalidEmployeeDataException("La identidad, el documento y el correo son obligatorios.");
        }
        requireText(firstName, "nombre");
        requireText(lastName, "apellido");
        requireText(position, "cargo");
        if (hireDate == null) {
            throw new InvalidEmployeeDataException("La fecha de contratación es obligatoria.");
        }
        if (currentDate != null && hireDate.isAfter(currentDate)) {
            throw new InvalidEmployeeDataException("La fecha de contratación no puede ser futura.");
        }
    }
    
    /** Crea un empleado nuevo validando que `hireDate` no sea futura respecto de `currentDate`. */
    public static Employee create(Document document, String firstName, String lastName,
                                  String email, String position, LocalDate hireDate,
                                  LocalDate currentDate) {
        return new Employee(
            EmployeeId.random(),
            document,
            firstName,
            lastName,
            new Email(email),
            position,
            hireDate,
            currentDate
        );
    }

    /** Reconstruye un empleado ya persistido: sus invariantes fueron validados al escribir. */
    public static Employee reconstitute(EmployeeId id, Document document, String firstName,
                                        String lastName, Email email, String position,
                                        LocalDate hireDate) {
        return new Employee(id, document, firstName, lastName, email, position, hireDate, null);
    }

    /** Actualiza los campos editables: solo los valores no nulos cambian. */
    public void update(String firstName, String lastName, String email, String position) {
        validateEditableFields(firstName, lastName, email, position);
        if (firstName != null) {
            this.firstName = firstName;
        }
        if (lastName != null) {
            this.lastName = lastName;
        }
        if (email != null) {
            this.email = new Email(email);
        }
        if (position != null) {
            this.position = position;
        }
    }

    private void validateEditableFields(String firstName, String lastName,
                                        String email, String position) {
        if (firstName != null) {
            requireText(firstName, "nombre");
        }
        if (lastName != null) {
            requireText(lastName, "apellido");
        }
        if (position != null) {
            requireText(position, "cargo");
        }
        if (email != null) {
            new Email(email);
        }
    }

    private static void requireText(String value, String fieldLabel) {
        if (value == null || value.isBlank() || value.length() > MAX_TEXT_LENGTH) {
            throw new InvalidEmployeeDataException("El " + fieldLabel + " debe tener entre "
                + MIN_TEXT_LENGTH + " y " + MAX_TEXT_LENGTH + " caracteres.");
        }
    }
    
    // Getters
    public EmployeeId getId() { return id; }
    public Document getDocument() { return document; }
    public DocumentType getDocumentType() { return document.type(); }
    public String getDocumentNumber() { return document.number(); }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public Email getEmail() { return email; }
    public String getPosition() { return position; }
    public LocalDate getHireDate() { return hireDate; }
}