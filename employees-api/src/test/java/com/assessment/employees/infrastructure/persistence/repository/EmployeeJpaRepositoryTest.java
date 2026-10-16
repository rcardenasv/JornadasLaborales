package com.assessment.employees.infrastructure.persistence.repository;

import com.assessment.employees.domain.model.Employee;
import com.assessment.employees.domain.model.EmployeeId;
import com.assessment.employees.infrastructure.persistence.entity.EmployeeEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeJpaRepositoryTest {
    @Mock EntityManager entityManager;
    @Mock TypedQuery<EmployeeEntity> employeeQuery;
    @Mock TypedQuery<Long> countQuery;
    @Mock Query mutationQuery;
    private EmployeeJpaRepository repository;

    @BeforeEach
    void setUp() {
        repository = new EmployeeJpaRepository(entityManager);
    }

    @Test
    void shouldMergeEmployeeOnSave() {
        Employee employee = employee();
        when(entityManager.merge(any(EmployeeEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertSame(employee, repository.save(employee));
        verify(entityManager).merge(argThat((EmployeeEntity entity) ->
            entity.getId().equals(employee.getId().value())));
    }

    @Test
    void shouldFindEmployeeByIdAndHandleAbsence() {
        EmployeeEntity entity = entity();
        UUID missingId = UUID.randomUUID();
        when(entityManager.find(EmployeeEntity.class, entity.getId())).thenReturn(entity);
        when(entityManager.find(EmployeeEntity.class, missingId)).thenReturn(null);

        assertEquals(entity.getId(), repository.findById(new EmployeeId(entity.getId())).orElseThrow()
            .getId().value());
        assertTrue(repository.findById(new EmployeeId(missingId)).isEmpty());
    }

    @Test
    void shouldFindByEmailAndDocumentNumber() {
        EmployeeEntity entity = entity();
        when(entityManager.createQuery(anyString(), eq(EmployeeEntity.class)))
            .thenReturn(employeeQuery)
            .thenReturn(employeeQuery);
        when(employeeQuery.setParameter(anyString(), any())).thenReturn(employeeQuery);
        when(employeeQuery.getResultStream()).thenReturn(Stream.of(entity)).thenReturn(Stream.empty());

        assertEquals(entity.getEmail(), repository.findByEmail(entity.getEmail()).orElseThrow()
            .getEmail().value());
        assertTrue(repository.findByDocumentNumber(entity.getDocumentNumber()).isEmpty());
        verify(employeeQuery, times(2)).setParameter(anyString(), any());
    }

    @Test
    void shouldPageCountAndCheckExistence() {
        EmployeeEntity entity = entity();
        when(entityManager.find(EmployeeEntity.class, entity.getId())).thenReturn(entity);
        when(entityManager.createQuery(anyString(), eq(EmployeeEntity.class))).thenReturn(employeeQuery);
        when(employeeQuery.setFirstResult(10)).thenReturn(employeeQuery);
        when(employeeQuery.setMaxResults(5)).thenReturn(employeeQuery);
        when(employeeQuery.getResultStream()).thenReturn(Stream.of(entity));
        when(entityManager.createQuery("SELECT COUNT(e) FROM EmployeeEntity e", Long.class)).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(21L);

        assertTrue(repository.existsById(new EmployeeId(entity.getId())));
        assertEquals(1, repository.findPage(10, 5).size());
        assertEquals(21L, repository.count());
        verify(employeeQuery).setFirstResult(10);
        verify(employeeQuery).setMaxResults(5);
    }

    @Test
    void shouldDeleteEmployeeAndWorkShifts() {
        EmployeeEntity entity = entity();
        when(entityManager.find(EmployeeEntity.class, entity.getId())).thenReturn(entity);
        when(entityManager.createQuery(anyString())).thenReturn(mutationQuery);
        when(mutationQuery.setParameter(anyString(), any())).thenReturn(mutationQuery);

        repository.deleteById(new EmployeeId(entity.getId()));
        repository.deleteWorkShiftsByEmployeeId(new EmployeeId(entity.getId()));

        verify(entityManager).remove(entity);
        verify(mutationQuery).executeUpdate();
    }

    private Employee employee() {
        return Employee.create("EMP001", "Ana", "Diaz", "ana@example.com", "Analyst", LocalDate.now());
    }

    private EmployeeEntity entity() {
        return new EmployeeEntity(UUID.randomUUID(), "EMP001", "Ana", "Diaz",
            "ana@example.com", "Analyst", LocalDate.now());
    }
}
