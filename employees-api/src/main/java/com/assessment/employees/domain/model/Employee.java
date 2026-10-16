package com.assessment.employees.domain.model;

import com.assessment.employees.domain.exception.InvalidEmployeeDataException;

import java.time.LocalDate;

/**
 * Employee aggregate root.
 * Represents a worker in the system.
 */
public class Employee {
    private final EmployeeId id;
    private final Document document;
    private String firstName;
    private String lastName;
    private Email email;
    private String position;
    private final LocalDate hireDate;
    
    public Employee(EmployeeId id, Document document, String firstName,
                   String lastName, Email email, String position, LocalDate hireDate) {
        this.id = id;
        this.document = document;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.position = position;
        this.hireDate = hireDate;
        
        validate();
    }
    
    private void validate() {
        if (id == null || document == null || email == null) {
            throw new InvalidEmployeeDataException("La identidad, el documento y el correo son obligatorios.");
        }
        if (firstName == null || firstName.isBlank() || firstName.length() > 100) {
            throw new InvalidEmployeeDataException("El nombre debe tener entre 1 y 100 caracteres.");
        }
        if (lastName == null || lastName.isBlank() || lastName.length() > 100) {
            throw new InvalidEmployeeDataException("El apellido debe tener entre 1 y 100 caracteres.");
        }
        if (position == null || position.isBlank() || position.length() > 100) {
            throw new InvalidEmployeeDataException("El cargo debe tener entre 1 y 100 caracteres.");
        }
        if (hireDate == null) {
            throw new InvalidEmployeeDataException("La fecha de contratación es obligatoria.");
        }
        if (hireDate.isAfter(LocalDate.now())) {
            throw new InvalidEmployeeDataException("La fecha de contratación no puede ser futura.");
        }
    }
    
    public static Employee create(Document document, String firstName, String lastName,
                                  String email, String position, LocalDate hireDate) {
        return new Employee(
            EmployeeId.random(),
            document,
            firstName,
            lastName,
            new Email(email),
            position,
            hireDate
        );
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

    public void update(String documentNumber, String firstName, String lastName,
                       String email, String position, LocalDate requestedHireDate) {
        if (!document.number().equals(documentNumber)) {
            throw new InvalidEmployeeDataException("El documento del empleado no se puede modificar.");
        }
        if (!hireDate.equals(requestedHireDate)) {
            throw new InvalidEmployeeDataException("La fecha de contratación no se puede modificar.");
        }
        update(firstName, lastName, email, position);
    }

    private void validateEditableFields(String firstName, String lastName,
                                        String email, String position) {
        if (firstName != null && (firstName.isBlank() || firstName.length() > 100)) {
            throw new InvalidEmployeeDataException("El nombre debe tener entre 1 y 100 caracteres.");
        }
        if (lastName != null && (lastName.isBlank() || lastName.length() > 100)) {
            throw new InvalidEmployeeDataException("El apellido debe tener entre 1 y 100 caracteres.");
        }
        if (position != null && (position.isBlank() || position.length() > 100)) {
            throw new InvalidEmployeeDataException("El cargo debe tener entre 1 y 100 caracteres.");
        }
        if (email != null) {
            try {
                new Email(email);
            } catch (IllegalArgumentException exception) {
                throw new InvalidEmployeeDataException("El correo electrónico no tiene un formato válido.");
            }
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