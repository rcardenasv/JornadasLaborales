package com.assessment.employees.infrastructure.persistence.repository;

import com.assessment.employees.domain.model.Document;
import com.assessment.employees.domain.model.WorkShift;
import com.assessment.employees.infrastructure.persistence.entity.WorkShiftEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkShiftJpaRepositoryTest {
    @Mock EntityManager entityManager;
    @Mock TypedQuery<WorkShiftEntity> shiftQuery;
    @Mock Query mutationQuery;
    private WorkShiftJpaRepository repository;

    @BeforeEach
    void setUp() {
        repository = new WorkShiftJpaRepository(entityManager);
    }

    @Test
    void shouldMergeAndFindShiftByUuid() {
        WorkShift shift = shift();
        when(entityManager.merge(any(WorkShiftEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(entityManager.find(WorkShiftEntity.class, shift.getId())).thenReturn(entity(shift));

        assertSame(shift, repository.save(shift));
        assertEquals(shift.getId(), repository.findById(shift.getId()).orElseThrow().getId());
    }

    @Test
    void shouldSaveMultipleShiftsInOneAdapterCall() {
        List<WorkShift> shifts = List.of(shift(), shift());
        when(entityManager.merge(any(WorkShiftEntity.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        assertEquals(shifts, repository.saveAll(shifts));

        verify(entityManager, times(2)).merge(any(WorkShiftEntity.class));
    }

    @Test
    void shouldFindAllShiftsAndOptionalDateBounds() {
        Document employeeDocument = Document.of("CC", "123456");
        WorkShiftEntity entity = entity(shift());
        when(entityManager.createQuery(anyString(), eq(WorkShiftEntity.class))).thenReturn(shiftQuery);
        when(shiftQuery.setParameter(anyString(), any())).thenReturn(shiftQuery);
        when(shiftQuery.getResultStream())
            .thenReturn(Stream.of(entity))
            .thenReturn(Stream.of(entity))
            .thenReturn(Stream.of(entity));

        assertEquals(1, repository.findByEmployeeDocument(employeeDocument).size());
        assertEquals(1, repository.findByEmployeeDocumentAndDateRange(employeeDocument, LocalDate.now(), null).size());
        assertEquals(1, repository.findByEmployeeDocumentAndDateRange(employeeDocument, null, LocalDate.now()).size());
        verify(shiftQuery, times(3)).setParameter("documentType", "CC");
        verify(shiftQuery, times(3)).setParameter("documentNumber", "123456");
    }

    @Test
    void shouldFindOverlapsAndDeleteShift() {
        WorkShiftEntity entity = entity(shift());
        when(entityManager.find(WorkShiftEntity.class, entity.getId())).thenReturn(entity);

        repository.deleteById(entity.getId());

        verify(entityManager).remove(entity);
    }

    @Test
    void shouldReturnEmptyWhenShiftDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(entityManager.find(WorkShiftEntity.class, id)).thenReturn(null);

        assertTrue(repository.findById(id).isEmpty());
    }

    private WorkShift shift() {
        return WorkShift.create(Document.of("CC", "123456"), LocalDate.now(),
            java.time.LocalTime.of(9, 0), java.time.LocalTime.of(17, 0));
    }

    private WorkShiftEntity entity(WorkShift shift) {
        return new WorkShiftEntity(shift.getId(), "CC", "123456",
            shift.getDate(), shift.getStartTime(), shift.getEndTime());
    }
}
