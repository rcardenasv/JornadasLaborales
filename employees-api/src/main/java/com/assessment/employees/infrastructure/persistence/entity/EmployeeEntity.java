package com.assessment.employees.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

/**
 * JPA entity for Employee.
 */
@Entity
@Table(name = "employees")
public class EmployeeEntity {
    
    @Id
    @Column(name = "id")
    private UUID id;
    
    @Column(name = "document_number", nullable = false, unique = true, length = 20)
    private String documentNumber;
    
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;
    
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;
    
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;
    
    @Column(name = "position", nullable = false, length = 100)
    private String position;
    
    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;
    
    // Default constructor
    public EmployeeEntity() {}
    
    public EmployeeEntity(UUID id, String documentNumber, String firstName, 
                         String lastName, String email, String position, LocalDate hireDate) {
        this.id = id;
        this.documentNumber = documentNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.position = position;
        this.hireDate = hireDate;
    }
    
    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getDocumentNumber() { return documentNumber; }
    public void setDocumentNumber(String documentNumber) { this.documentNumber = documentNumber; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    
    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
}