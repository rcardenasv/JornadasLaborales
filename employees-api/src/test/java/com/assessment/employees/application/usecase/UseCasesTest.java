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

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UseCasesTest {
    private static final String TYPE = "PA";
    private static final String NUMBER = "EMP001";
    private static final Document DOCUMENT = Document.of(TYPE, NUMBER);
    private static final LocalDate TODAY = LocalDate.of(2026, 1, 1);
    private static final Clock FIXED_CLOCK = Clock.fixed(
        java.time.Instant.parse("2026-01-01T12:00:00Z"), java.time.ZoneOffset.UTC);

    @Mock EmployeeRepository employees;
    @Mock WorkShiftRepository shifts;

    private CreateEmployee createEmployee() {
        return new CreateEmployee(employees, FIXED_CLOCK);
    }

    @Test
    void createEmployeeChecksDuplicatesAndPersists() {
        when(employees.findByEmail("ana@example.com")).thenReturn(Optional.empty());
        when(employees.findByDocument(DOCUMENT)).thenReturn(Optional.empty());
        when(employees.save(any())).thenAnswer(call -> call.getArgument(0));

        EmployeeResult result = createEmployee().execute(employeeCommand());

        assertEquals(NUMBER, result.documentNumber());
        assertEquals(TYPE, result.documentType());
        verify(employees).save(any(Employee.class));
    }

    @Test
    void createEmployeeRejectsDuplicateEmailAndDocument() {
        when(employees.findByEmail("ana@example.com")).thenReturn(Optional.of(employee()));
        assertThrows(DuplicateEmployeeException.class,
            () -> createEmployee().execute(employeeCommand()));
        verify(employees, never()).findByDocument(any(Document.class));

        reset(employees);
        when(employees.findByEmail("ana@example.com")).thenReturn(Optional.empty());
        when(employees.findByDocument(DOCUMENT)).thenReturn(Optional.of(employee()));
        assertThrows(DuplicateEmployeeException.class,
            () -> createEmployee().execute(employeeCommand()));
    }

    @Test
    void allowsSameDocumentNumberForDifferentTypes() {
        when(employees.findByEmail("cc@example.com")).thenReturn(Optional.empty());
        when(employees.findByEmail("ce@example.com")).thenReturn(Optional.empty());
        when(employees.findByDocument(Document.of("CC", "111"))).thenReturn(Optional.empty());
        when(employees.findByDocument(Document.of("CE", "111"))).thenReturn(Optional.empty());
        when(employees.save(any())).thenAnswer(call -> call.getArgument(0));

        CreateEmployee create = createEmployee();
        EmployeeResult cc = create.execute(new CreateEmployeeCommand(
            Document.of("CC", "111"), "Ana", "Diaz", "cc@example.com", "Analyst", TODAY
        ));
        EmployeeResult ce = create.execute(new CreateEmployeeCommand(
            Document.of("CE", "111"), "Bea", "Lopez", "ce@example.com", "Analyst", TODAY
        ));

        assertEquals(cc.documentNumber(), ce.documentNumber());
        assertNotEquals(cc.documentType(), ce.documentType());
    }

    @Test
    void getEmployeeFindsByNaturalDocumentIdentityOrThrows() {
        Employee existing = employee();
        when(employees.findByDocument(DOCUMENT)).thenAnswer(call -> Optional.of(existing))
            .thenAnswer(call -> Optional.empty());
        GetEmployee useCase = new GetEmployee(employees);

        assertEquals(NUMBER, useCase.execute(DOCUMENT).documentNumber());
        assertThrows(EmployeeNotFoundException.class, () -> useCase.execute(DOCUMENT));
    }

    @Test
    void listEmployeesUsesPageAndTotalAndRejectsInvalidParameters() {
        when(employees.findPage(20, 10)).thenReturn(List.of(employee()));
        when(employees.count()).thenReturn(25L);
        ListEmployees useCase = new ListEmployees(employees);

        EmployeePageResult page = useCase.execute(2, 10);
        assertEquals(25L, page.totalItems());
        assertEquals(1, page.items().size());
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(-1, 10));
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(Integer.MAX_VALUE, 100));
    }

    @Test
    void updateEmployeeAllowsSameValuesAndRejectsMissingDuplicateOrChangedHireDate() {
        Employee current = employee();
        UpdateEmployee useCase = new UpdateEmployee(employees);
        UpdateEmployeeCommand same = employeeUpdate(TODAY);
        when(employees.findByDocument(DOCUMENT)).thenAnswer(call -> Optional.of(current))
            .thenAnswer(call -> Optional.empty())
            .thenAnswer(call -> Optional.of(current))
            .thenAnswer(call -> Optional.of(current));
        when(employees.findByEmail("ana@example.com")).thenReturn(Optional.of(current));
        when(employees.findByEmail("other@example.com")).thenReturn(Optional.of(
            Employee.create(Document.of("CC", "123"), "Bea", "Lopez", "other@example.com",
                "Analyst", TODAY, TODAY)
        ));
        when(employees.save(current)).thenReturn(current);

        assertEquals("Ana", useCase.execute(same).firstName());
        assertThrows(EmployeeNotFoundException.class, () -> useCase.execute(same));
        assertThrows(DuplicateEmployeeException.class,
            () -> useCase.execute(employeeUpdate("other@example.com", TODAY)));
        assertThrows(InvalidEmployeeDataException.class,
            () -> useCase.execute(employeeUpdate(LocalDate.now().minusDays(1))));
        verify(employees, times(1)).save(current);    }

    @Test
    void updateEmployeeAcceptsPartialFieldsAndRequiresAtLeastOne() {
        Employee current = employee();
        UpdateEmployee useCase = new UpdateEmployee(employees);
        when(employees.findByDocument(DOCUMENT)).thenReturn(Optional.of(current));
        when(employees.save(current)).thenReturn(current);

        EmployeeResult result = useCase.execute(new UpdateEmployeeCommand(
            DOCUMENT, null, null, null, "Senior Analyst", null));

        assertEquals("Senior Analyst", result.position());
        assertEquals("Ana", result.firstName());
        assertEquals("ana@example.com", result.email());
        verify(employees, never()).findByEmail(any());

        assertThrows(InvalidEmployeeDataException.class, () -> useCase.execute(
            new UpdateEmployeeCommand(DOCUMENT, null, null, null, null, null)));
        verify(employees, times(1)).save(current);
    }

    @Test
    void updateEmployeeValidatesHireDateOnlyWhenProvided() {
        Employee current = employee();
        UpdateEmployee useCase = new UpdateEmployee(employees);
        when(employees.findByDocument(DOCUMENT)).thenAnswer(call -> Optional.of(current))
            .thenAnswer(call -> Optional.of(current));
        when(employees.save(current)).thenReturn(current);

        useCase.execute(new UpdateEmployeeCommand(
            DOCUMENT, "Ana Dos", null, null, null, null));
        assertEquals("Ana Dos", current.getFirstName());

        assertThrows(InvalidEmployeeDataException.class, () -> useCase.execute(
            new UpdateEmployeeCommand(DOCUMENT, null, null, "ana@example.com",
                null, LocalDate.now().minusDays(1))));
    }

    @Test
    void deleteEmployeeUsesDocumentAndRemovesAssociatedShifts() {
        Employee existing = employee();
        when(employees.findByDocument(DOCUMENT)).thenAnswer(call -> Optional.of(existing))
            .thenAnswer(call -> Optional.empty());
        DeleteEmployee useCase = new DeleteEmployee(employees);

        useCase.execute(DOCUMENT);

        verify(employees).deleteById(existing.getId());
        assertThrows(EmployeeNotFoundException.class, () -> useCase.execute(DOCUMENT));
    }

    @Test
    void createWorkShiftChecksEmployeeOverlapAndPersists() {
        CreateWorkShiftCommand command = shiftCommand();
        when(employees.findByDocument(DOCUMENT)).thenReturn(Optional.of(employee()));
        when(shifts.findByEmployeeDocumentAndDateRange(eq(DOCUMENT), any(), any())).thenReturn(List.of());
        when(shifts.save(any())).thenAnswer(call -> call.getArgument(0));
        CreateWorkShift useCase = new CreateWorkShift(shifts, employees);

        WorkShiftResult result = useCase.execute(command);
        assertEquals(command.startTime(), result.startTime());

        WorkShift overlapping = shift();
        when(shifts.findByEmployeeDocumentAndDateRange(eq(DOCUMENT), any(), any()))
            .thenReturn(List.of(overlapping));
        assertThrows(WorkShiftOverlapException.class, () -> useCase.execute(command));
        when(employees.findByDocument(DOCUMENT)).thenReturn(Optional.empty());
        assertThrows(EmployeeNotFoundException.class, () -> useCase.execute(command));
    }

    @Test
    void getShiftChecksUuidAndDocumentOwnership() {
        WorkShift existing = shift();
        when(shifts.findById(existing.getId())).thenReturn(Optional.of(existing));
        GetWorkShift useCase = new GetWorkShift(shifts);

        assertEquals(existing.getId(),
            useCase.execute(existing.getId(), DOCUMENT).id());
        assertThrows(WorkShiftNotFoundException.class,
            () -> useCase.execute(existing.getId(), Document.of("CC", "123")));
        when(shifts.findById(existing.getId())).thenReturn(Optional.empty());
        assertThrows(WorkShiftNotFoundException.class,
            () -> useCase.execute(existing.getId(), DOCUMENT));
    }

    @Test
    void listShiftsSupportsBoundsAndRejectsMissingEmployeeOrInvertedRange() {
        when(employees.findByDocument(DOCUMENT)).thenAnswer(call -> Optional.of(employee()))
            .thenAnswer(call -> Optional.of(employee()))
            .thenAnswer(call -> Optional.of(employee()))
            .thenAnswer(call -> Optional.empty());
        when(shifts.findByEmployeeDocument(DOCUMENT)).thenReturn(List.of());
        when(shifts.findByEmployeeDocumentAndDateRange(eq(DOCUMENT), any(), any())).thenReturn(List.of());
        ListEmployeeWorkShifts useCase = new ListEmployeeWorkShifts(shifts, employees);

        assertTrue(useCase.execute(DOCUMENT, null, null).shifts().isEmpty());
        assertTrue(useCase.execute(DOCUMENT, LocalDate.now(), null).shifts().isEmpty());
        assertThrows(InvalidDateRangeException.class,
            () -> useCase.execute(DOCUMENT, LocalDate.now(), LocalDate.now().minusDays(1)));
        assertThrows(EmployeeNotFoundException.class,
            () -> useCase.execute(DOCUMENT, null, null));
    }

    @Test
    void weeklySummaryUsesWholeIsoWeekAndSplitsSundayNightHours() {
        LocalDate monday = LocalDate.of(2026, 10, 19);
        WorkShift sundayNight = WorkShift.create(DOCUMENT, monday.minusDays(1),
            LocalTime.of(22, 0), LocalTime.of(6, 0));
        WorkShift mondayShift = WorkShift.create(DOCUMENT, monday,
            LocalTime.of(9, 0), LocalTime.of(17, 0));
        when(employees.findByDocument(DOCUMENT)).thenReturn(Optional.of(employee()));
        when(shifts.findByEmployeeDocumentAndDateRange(
            DOCUMENT, monday.minusDays(1), monday.plusDays(6)
        )).thenReturn(List.of(sundayNight, mondayShift));

        WorkShiftListResult result = new ListEmployeeWorkShifts(shifts, employees)
            .execute(DOCUMENT, monday, monday);

        assertEquals(1, result.shifts().size());
        assertEquals(Duration.ofHours(14), result.weeklySummaries().get(0).scheduledHours());
        assertEquals(monday, result.weeklySummaries().get(0).weekStart());
    }

    @Test
    void bulkCreateSavesOnlySelectedWeekdays() {
        LocalDate monday = LocalDate.of(2026, 10, 19);
        BulkCreateWorkShifts useCase = new BulkCreateWorkShifts(shifts, employees);
        when(employees.findByDocument(DOCUMENT)).thenReturn(Optional.of(employee()));
        when(shifts.findByEmployeeDocumentAndDateRange(eq(DOCUMENT), any(), any())).thenReturn(List.of());
        when(shifts.saveAll(anyList())).thenAnswer(call -> call.getArgument(0));
        BulkCreateWorkShiftsResult result = useCase.execute(new BulkCreateWorkShiftsCommand(
            DOCUMENT, monday, monday.plusDays(4),
            java.util.Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
            LocalTime.of(9, 0), LocalTime.of(17, 0)
        ));

        assertEquals(3, result.createdShifts().size());
        assertEquals(Duration.ofHours(24), result.scheduledHours());
        assertEquals(List.of(monday, monday.plusDays(2), monday.plusDays(4)),
            result.createdShifts().stream().map(WorkShiftResult::date).toList());
        verify(shifts).saveAll(anyList());
    }

    @Test
    void bulkCreateIsAtomicWhenAnyDateConflicts() {
        LocalDate monday = LocalDate.of(2026, 10, 19);
        WorkShift existing = WorkShift.create(DOCUMENT, monday.plusDays(1),
            LocalTime.of(9, 0), LocalTime.of(17, 0));
        when(employees.findByDocument(DOCUMENT)).thenReturn(Optional.of(employee()));
        when(shifts.findByEmployeeDocumentAndDateRange(eq(DOCUMENT), any(), any()))
            .thenReturn(List.of(existing));

        WorkShiftBulkConflictException exception = assertThrows(WorkShiftBulkConflictException.class,
            () -> new BulkCreateWorkShifts(shifts, employees).execute(new BulkCreateWorkShiftsCommand(
                DOCUMENT, monday, monday.plusDays(2), null,
                LocalTime.of(9, 0), LocalTime.of(17, 0)
            )));

        assertEquals(List.of(monday.plusDays(1)), exception.getConflictDates());
        verify(shifts, never()).saveAll(anyList());
    }

    @Test
    void bulkCreateRejectsWeeklyLimitAndInvalidRangesWithoutSaving() {
        LocalDate monday = LocalDate.of(2026, 10, 19);
        List<WorkShift> existing = java.util.stream.IntStream.range(0, 5)
            .mapToObj(day -> WorkShift.create(DOCUMENT, monday.plusDays(day),
                LocalTime.of(9, 0), LocalTime.of(17, 0)))
            .toList();
        when(employees.findByDocument(DOCUMENT)).thenReturn(Optional.of(employee()));
        when(shifts.findByEmployeeDocumentAndDateRange(eq(DOCUMENT), any(), any())).thenReturn(existing);
        BulkCreateWorkShifts useCase = new BulkCreateWorkShifts(shifts, employees);

        WorkShiftBulkConflictException weeklyConflict = assertThrows(WorkShiftBulkConflictException.class,
            () -> useCase.execute(new BulkCreateWorkShiftsCommand(
                DOCUMENT, monday.plusDays(5), monday.plusDays(5), null,
                LocalTime.of(9, 0), LocalTime.MIDNIGHT
            )));
        assertEquals(List.of(monday.plusDays(5)), weeklyConflict.getConflictDates());
        assertThrows(InvalidDateRangeException.class, () -> useCase.execute(new BulkCreateWorkShiftsCommand(
            DOCUMENT, monday.plusDays(1), monday, null, LocalTime.of(9, 0), LocalTime.of(17, 0)
        )));
        assertThrows(InvalidDateRangeException.class, () -> useCase.execute(new BulkCreateWorkShiftsCommand(
            DOCUMENT, monday, monday.plusDays(2), java.util.Set.of(),
            LocalTime.of(9, 0), LocalTime.of(17, 0)
        )));
        verify(shifts, never()).saveAll(anyList());
    }

    @Test
    void bulkCreateRejectsWhenNoDatesMatchSelectedWeekdays() {
        LocalDate monday = LocalDate.of(2026, 10, 19);
        when(employees.findByDocument(DOCUMENT)).thenReturn(Optional.of(employee()));

        assertThrows(InvalidDateRangeException.class,
            () -> new BulkCreateWorkShifts(shifts, employees).execute(new BulkCreateWorkShiftsCommand(
                DOCUMENT, monday, monday.plusDays(2), java.util.Set.of(DayOfWeek.SUNDAY),
                LocalTime.of(9, 0), LocalTime.of(17, 0)
            )));
        verify(shifts, never()).saveAll(anyList());
    }

    @Test
    void updateShiftIgnoresItselfButRejectsOtherOverlapsAndMissingResources() {
        WorkShift current = shift();
        UpdateWorkShiftCommand command = new UpdateWorkShiftCommand(current.getId(), DOCUMENT,
            current.getDate(), LocalTime.of(10, 0), LocalTime.of(18, 0));
        when(shifts.findById(current.getId())).thenReturn(Optional.of(current));
        when(employees.findByDocument(DOCUMENT)).thenReturn(Optional.of(employee()));
        when(shifts.findByEmployeeDocumentAndDateRange(eq(DOCUMENT), any(), any()))
            .thenReturn(List.of(current));
        when(shifts.save(any(WorkShift.class))).thenAnswer(call -> call.getArgument(0));
        UpdateWorkShift useCase = new UpdateWorkShift(shifts, employees);

        assertEquals(LocalTime.of(10, 0), useCase.execute(command).startTime());

        WorkShift other = shift();
        when(shifts.findByEmployeeDocumentAndDateRange(eq(DOCUMENT), any(), any()))
            .thenReturn(List.of(other));
        assertThrows(WorkShiftOverlapException.class, () -> useCase.execute(command));
        when(employees.findByDocument(DOCUMENT)).thenReturn(Optional.empty());
        assertThrows(EmployeeNotFoundException.class, () -> useCase.execute(command));
        when(shifts.findById(current.getId())).thenReturn(Optional.empty());
        assertThrows(WorkShiftNotFoundException.class, () -> useCase.execute(command));
    }

    @Test
    void deleteShiftValidatesOwnershipAndEmployee() {
        WorkShift existing = shift();
        when(shifts.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(employees.findByDocument(DOCUMENT)).thenReturn(Optional.of(employee()));
        DeleteWorkShift useCase = new DeleteWorkShift(shifts, employees);

        useCase.execute(existing.getId(), DOCUMENT);
        verify(shifts).deleteById(existing.getId());
        assertThrows(WorkShiftNotFoundException.class,
            () -> useCase.execute(existing.getId(), Document.of("CC", "123")));
        when(employees.findByDocument(DOCUMENT)).thenReturn(Optional.empty());
        assertThrows(EmployeeNotFoundException.class,
            () -> useCase.execute(existing.getId(), DOCUMENT));
    }

    private CreateEmployeeCommand employeeCommand() {
        return new CreateEmployeeCommand(DOCUMENT, "Ana", "Diaz", "ana@example.com",
            "Analyst", TODAY);
    }

    private UpdateEmployeeCommand employeeUpdate(LocalDate hireDate) {
        return employeeUpdate("ana@example.com", hireDate);
    }

    private UpdateEmployeeCommand employeeUpdate(String email, LocalDate hireDate) {
        return new UpdateEmployeeCommand(DOCUMENT, "Ana", "Diaz", email, "Analyst", hireDate);
    }

    private Employee employee() {
        return Employee.create(DOCUMENT, "Ana", "Diaz", "ana@example.com", "Analyst", TODAY, TODAY);
    }

    private CreateWorkShiftCommand shiftCommand() {
        return new CreateWorkShiftCommand(DOCUMENT, LocalDate.now(),
            LocalTime.of(9, 0), LocalTime.of(17, 0));
    }

    private WorkShift shift() {
        return WorkShift.create(DOCUMENT, LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(17, 0));
    }
}
