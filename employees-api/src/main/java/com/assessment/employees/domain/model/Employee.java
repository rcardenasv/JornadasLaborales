package com.assessment.employees.domain.model;

import java.time.LocalDate;

/**
 * Employee aggregate root.
 * Represents a worker in the system.
 */
public class Employee {
    private EmployeeId id;
    private DocumentNumber documentNumber;
    private String firstName;
    private String lastName;
    private Email email;
    private String position;
    private LocalDate hireDate;
    
    public Employee(EmployeeId id, DocumentNumber documentNumber, String firstName, 
                   String lastName, Email email, String position, LocalDate hireDate) {
        this.id = id;
        this.documentNumber = documentNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.position = position;
        this.hireDate = hireDate;
        
        validate();
    }
    
    private void validate() {
        if (firstName == null || firstName.isBlank() || firstName.length() > 100) {
            throw new IllegalArgumentException("First name must be between 1 and 100 characters");
        }
        if (lastName == null || lastName.isBlank() || lastName.length() > 100) {
            throw new IllegalArgumentException("Last name must be between 1 and 100 characters");
        }
        if (position == null || position.isBlank() || position.length() > 100) {
            throw new IllegalArgumentException("Position must be between 1 and 100 characters");
        }
        if (hireDate == null) {
            throw new IllegalArgumentException("Hire date cannot be null");
        }
        if (hireDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Hire date cannot be in the future");
        }
    }
    
    public static Employee create(String documentNumber, String firstName, String lastName, 
                                  String email, String position, LocalDate hireDate) {
        return new Employee(
            EmployeeId.random(),
            new DocumentNumber(documentNumber),
            firstName,
            lastName,
            new Email(email),
            position,
            hireDate
        );
    }
    
    public void update(String documentNumber, String firstName, String lastName,
                      String email, String position, LocalDate hireDate) {
        Employee updated = new Employee(
            id,
            new DocumentNumber(documentNumber),
            firstName,
            lastName,
            new Email(email),
            position,
            hireDate
        );
        this.documentNumber = updated.documentNumber;
        this.firstName = updated.firstName;
        this.lastName = updated.lastName;
        this.email = updated.email;
        this.position = updated.position;
        this.hireDate = updated.hireDate;
    }
    
    // Getters
    public EmployeeId getId() { return id; }
    public DocumentNumber getDocumentNumber() { return documentNumber; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public Email getEmail() { return email; }
    public String getPosition() { return position; }
    public LocalDate getHireDate() { return hireDate; }
}