package com.assessment.employees.application.usecase;

import com.assessment.employees.application.dto.*;
import com.assessment.employees.domain.exception.*;
import com.assessment.employees.domain.model.*;
import com.assessment.employees.domain.repository.EmployeeRepository;
import com.assessment.employees.domain.repository.WorkShiftRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UseCasesTest {
    @Mock EmployeeRepository employees;
    @Mock WorkShiftRepository shifts;

    @Test
    void createEmployeeChecksDuplicatesAndPersists() {
        when(employees.findByEmail("ana@example.com")).thenReturn(Optional.empty());
        when(employees.findByDocumentNumber("EMP001")).thenReturn(Optional.empty());
        when(employees.save(any())).thenAnswer(call -> call.getArgument(0));

        var result = new CreateEmployee(employees).execute(employeeCommand());

        assertEquals("EMP001", result.documentNumber());
        verify(employees).save(any(Employee.class));
    }

    @Test
    void createEmployeeRejectsDuplicateEmailAndDocument() {
        when(employees.findByEmail("ana@example.com")).thenReturn(Optional.of(employee()));
        assertThrows(DuplicateEmployeeException.class,
            () -> new CreateEmployee(employees).execute(employeeCommand()));
        verify(employees, never()).findByDocumentNumber(anyString());

        reset(employees);
        when(employees.findByEmail("ana@example.com")).thenReturn(Optional.empty());
        when(employees.findByDocumentNumber("EMP001")).thenReturn(Optional.of(employee()));
        assertThrows(DuplicateEmployeeException.class,
            () -> new CreateEmployee(employees).execute(employeeCommand()));
    }

    @Test
    void getEmployeeReturnsValueOrNotFound() {
        Employee employee = employee();
        when(employees.findById(employee.getId())).thenReturn(Optional.of(employee));
        assertEquals("Ana", new GetEmployee(employees).execute(employee.getId().toString()).firstName());
        when(employees.findById(employee.getId())).thenReturn(Optional.empty());
        assertThrows(EmployeeNotFoundException.class,
            () -> new GetEmployee(employees).execute(employee.getId().toString()));
    }

    @Test
    void listEmployeesUsesPageAndTotalAndRejectsInvalidParameters() {
        when(employees.findPage(20, 10)).thenReturn(List.of(employee()));
        when(employees.count()).thenReturn(25L);
        var page = new ListEmployees(employees).execute(2, 10);
        assertEquals(25L, page.totalItems());
        assertEquals(1, page.items().size());
        assertThrows(IllegalArgumentException.class, () -> new ListEmployees(employees).execute(-1, 10));
        assertThrows(IllegalArgumentException.class, () -> new ListEmployees(employees).execute(Integer.MAX_VALUE, 100));
    }

    @Test
    void updateEmployeeAllowsSameValuesAndRejectsMissingOrDuplicates() {
        Employee current = employee();
        var useCase = new UpdateEmployee(employees);
        UpdateEmployeeCommand same = new UpdateEmployeeCommand(current.getId().toString(), "EMP001", "Ana",
            "Diaz", "ana@example.com", "Analyst", LocalDate.now());
        when(employees.findById(current.getId())).thenReturn(Optional.of(current));
        when(employees.findByEmail("ana@example.com")).thenReturn(Optional.of(current));
        when(employees.findByDocumentNumber("EMP001")).thenReturn(Optional.of(current));
        when(employees.save(current)).thenReturn(current);
        assertEquals("Ana", useCase.execute(same).firstName());

        when(employees.findById(current.getId())).thenReturn(Optional.empty());
        assertThrows(EmployeeNotFoundException.class, () -> useCase.execute(same));

        Employee duplicate = Employee.create("EMP002", "Bea", "Lopez", "bea@example.com", "Analyst", LocalDate.now());
        when(employees.findById(current.getId())).thenReturn(Optional.of(current));
        when(employees.findByEmail("bea@example.com")).thenReturn(Optional.of(duplicate));
        assertThrows(DuplicateEmployeeException.class, () -> useCase.execute(
            new UpdateEmployeeCommand(current.getId().toString(), "EMP003", "Bea", "Lopez",
                "bea@example.com", "Analyst", LocalDate.now())));
    }

    @Test
    void deleteEmployeeChecksExistenceAndRemovesShifts() {
        EmployeeId id = employee().getId();
        when(employees.existsById(id)).thenReturn(true);
        new DeleteEmployee(employees).execute(id.toString());
        verify(employees).deleteWorkShiftsByEmployeeId(id);
        verify(employees).deleteById(id);
        when(employees.existsById(id)).thenReturn(false);
        assertThrows(EmployeeNotFoundException.class, () -> new DeleteEmployee(employees).execute(id.toString()));
    }

    @Test
    void createWorkShiftValidatesEmployeeOverlapAndPersists() {
        EmployeeId employeeId = EmployeeId.random();
        var command = shiftCommand(employeeId);
        when(employees.existsById(employeeId)).thenReturn(true);
        when(shifts.findOverlapping(eq(employeeId), any(), any(), any())).thenReturn(List.of());
        when(shifts.save(any())).thenAnswer(call -> call.getArgument(0));
        assertEquals(employeeId.value().toString(), new CreateWorkShift(shifts, employees).execute(command).employeeId());

        when(employees.existsById(employeeId)).thenReturn(false);
        assertThrows(EmployeeNotFoundException.class, () -> new CreateWorkShift(shifts, employees).execute(command));
        when(employees.existsById(employeeId)).thenReturn(true);
        when(shifts.findOverlapping(eq(employeeId), any(), any(), any())).thenReturn(List.of(shift(employeeId)));
        assertThrows(WorkShiftOverlapException.class, () -> new CreateWorkShift(shifts, employees).execute(command));
    }

    @Test
    void getShiftChecksUuidAndOwnership() {
        EmployeeId employeeId = EmployeeId.random();
        WorkShift shift = shift(employeeId);
        when(shifts.findById(shift.getId())).thenReturn(Optional.of(shift));
        assertEquals(shift.getId(), new GetWorkShift(shifts).execute(shift.getId().toString(),
            employeeId.value().toString()).id());
        assertThrows(WorkShiftNotFoundException.class, () -> new GetWorkShift(shifts).execute(
            shift.getId().toString(), UUID.randomUUID().toString()));
        when(shifts.findById(shift.getId())).thenReturn(Optional.empty());
        assertThrows(WorkShiftNotFoundException.class, () -> new GetWorkShift(shifts).execute(
            shift.getId().toString(), employeeId.value().toString()));
    }

    @Test
    void listShiftsSupportsBoundsAndRejectsMissingEmployeeOrInvertedRange() {
        EmployeeId id = EmployeeId.random();
        when(employees.existsById(id)).thenReturn(true);
        when(shifts.findByEmployeeId(id)).thenReturn(List.of());
        when(shifts.findByEmployeeIdAndDateRange(eq(id), any(), any())).thenReturn(List.of());
        var useCase = new ListEmployeeWorkShifts(shifts, employees);
        assertTrue(useCase.execute(id.toString(), null, null).isEmpty());
        assertTrue(useCase.execute(id.toString(), LocalDate.now(), null).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(id.toString(),
            LocalDate.now(), LocalDate.now().minusDays(1)));
        when(employees.existsById(id)).thenReturn(false);
        assertThrows(EmployeeNotFoundException.class, () -> useCase.execute(id.toString(), null, null));
    }

    @Test
    void updateShiftIgnoresItselfButRejectsOtherOverlapsAndMissingParent() {
        EmployeeId employeeId = EmployeeId.random();
        WorkShift current = shift(employeeId);
        UpdateWorkShiftCommand command = new UpdateWorkShiftCommand(current.getId(),
            employeeId.toString(), current.getDate(), LocalTime.of(10, 0), LocalTime.of(18, 0));
        when(shifts.findById(current.getId())).thenReturn(Optional.of(current));
        when(employees.existsById(employeeId)).thenReturn(true);
        when(shifts.findOverlapping(eq(employeeId), any(), any(), any())).thenReturn(List.of(current));
        when(shifts.save(current)).thenReturn(current);
        assertEquals(LocalTime.of(10, 0), new UpdateWorkShift(shifts, employees).execute(command).startTime());

        WorkShift other = shift(employeeId);
        when(shifts.findOverlapping(eq(employeeId), any(), any(), any())).thenReturn(List.of(other));
        assertThrows(WorkShiftOverlapException.class, () -> new UpdateWorkShift(shifts, employees).execute(command));
        when(employees.existsById(employeeId)).thenReturn(false);
        assertThrows(EmployeeNotFoundException.class, () -> new UpdateWorkShift(shifts, employees).execute(command));
        when(shifts.findById(current.getId())).thenReturn(Optional.empty());
        assertThrows(WorkShiftNotFoundException.class, () -> new UpdateWorkShift(shifts, employees).execute(command));
    }

    @Test
    void deleteShiftValidatesParentAndOwnership() {
        EmployeeId owner = EmployeeId.random();
        WorkShift shift = shift(owner);
        when(shifts.findById(shift.getId())).thenReturn(Optional.of(shift));
        when(employees.existsById(owner)).thenReturn(true);
        var useCase = new DeleteWorkShift(shifts, employees);
        useCase.execute(shift.getId().toString(), owner.toString());
        verify(shifts).deleteById(shift.getId());
        assertThrows(WorkShiftNotFoundException.class, () -> useCase.execute(
            shift.getId().toString(), UUID.randomUUID().toString()));
        when(employees.existsById(owner)).thenReturn(false);
        assertThrows(EmployeeNotFoundException.class, () -> useCase.execute(
            shift.getId().toString(), owner.toString()));
    }

    private CreateEmployeeCommand employeeCommand() {
        return new CreateEmployeeCommand("EMP001", "Ana", "Diaz", "ana@example.com", "Analyst", LocalDate.now());
    }

    private Employee employee() {
        return Employee.create("EMP001", "Ana", "Diaz", "ana@example.com", "Analyst", LocalDate.now());
    }

    private CreateWorkShiftCommand shiftCommand(EmployeeId employeeId) {
        return new CreateWorkShiftCommand(employeeId.toString(), LocalDate.now(),
            LocalTime.of(9, 0), LocalTime.of(17, 0));
    }

    private WorkShift shift(EmployeeId employeeId) {
        return WorkShift.create(employeeId, LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(17, 0));
    }
}
