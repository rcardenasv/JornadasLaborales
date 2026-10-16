package com.assessment.employees.infrastructure.persistence.repository;

import com.assessment.employees.domain.model.EmployeeId;
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
import java.time.LocalTime;
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
        WorkShift shift = shift(EmployeeId.random());
        when(entityManager.merge(any(WorkShiftEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(entityManager.find(WorkShiftEntity.class, shift.getId())).thenReturn(entity(shift));

        assertSame(shift, repository.save(shift));
        assertEquals(shift.getId(), repository.findById(shift.getId()).orElseThrow().getId());
    }

    @Test
    void shouldFindAllShiftsAndOptionalDateBounds() {
        EmployeeId employeeId = EmployeeId.random();
        WorkShiftEntity entity = entity(shift(employeeId));
        when(entityManager.createQuery(anyString(), eq(WorkShiftEntity.class))).thenReturn(shiftQuery);
        when(shiftQuery.setParameter(anyString(), any())).thenReturn(shiftQuery);
        when(shiftQuery.getResultStream())
            .thenReturn(Stream.of(entity))
            .thenReturn(Stream.of(entity))
            .thenReturn(Stream.of(entity));

        assertEquals(1, repository.findByEmployeeId(employeeId).size());
        assertEquals(1, repository.findByEmployeeIdAndDateRange(employeeId, LocalDate.now(), null).size());
        assertEquals(1, repository.findByEmployeeIdAndDateRange(employeeId, null, LocalDate.now()).size());
        verify(shiftQuery, times(3)).setParameter("empId", employeeId.value());
    }

    @Test
    void shouldFindOverlapsAndDeleteShift() {
        EmployeeId employeeId = EmployeeId.random();
        WorkShiftEntity entity = entity(shift(employeeId));
        when(entityManager.createQuery(anyString(), eq(WorkShiftEntity.class))).thenReturn(shiftQuery);
        when(shiftQuery.setParameter(anyString(), any())).thenReturn(shiftQuery);
        when(shiftQuery.getResultStream()).thenReturn(Stream.of(entity));
        when(entityManager.find(WorkShiftEntity.class, entity.getId())).thenReturn(entity);
        when(entityManager.createQuery(anyString())).thenReturn(mutationQuery);
        when(mutationQuery.setParameter(anyString(), any())).thenReturn(mutationQuery);

        assertEquals(1, repository.findOverlapping(employeeId, entity.getDate(),
            LocalTime.of(8, 0), LocalTime.of(10, 0)).size());
        repository.deleteById(entity.getId());
        repository.deleteByEmployeeId(employeeId);

        verify(entityManager).remove(entity);
        verify(mutationQuery).executeUpdate();
    }

    @Test
    void shouldReturnEmptyWhenShiftDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(entityManager.find(WorkShiftEntity.class, id)).thenReturn(null);

        assertTrue(repository.findById(id).isEmpty());
    }

    private WorkShift shift(EmployeeId employeeId) {
        return WorkShift.create(employeeId, LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(17, 0));
    }

    private WorkShiftEntity entity(WorkShift shift) {
        return new WorkShiftEntity(shift.getId(), shift.getEmployeeId().value(),
            shift.getDate(), shift.getStartTime(), shift.getEndTime());
    }
}
