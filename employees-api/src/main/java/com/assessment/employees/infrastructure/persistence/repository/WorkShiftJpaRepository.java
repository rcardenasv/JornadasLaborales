package com.assessment.employees.infrastructure.persistence.repository;

import com.assessment.employees.domain.model.EmployeeId;
import com.assessment.employees.domain.model.WorkShift;
import com.assessment.employees.domain.repository.WorkShiftRepository;
import com.assessment.employees.infrastructure.persistence.entity.WorkShiftEntity;
import com.assessment.employees.infrastructure.persistence.mapper.WorkShiftMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA implementation of WorkShiftRepository.
 */
@ApplicationScoped
public class WorkShiftJpaRepository implements WorkShiftRepository {
    private final EntityManager entityManager;

    @Inject
    public WorkShiftJpaRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    
    @Override
    @Transactional
    public WorkShift save(WorkShift workShift) {
        WorkShiftEntity entity = WorkShiftMapper.toEntity(workShift);
        entityManager.merge(entity);
        return workShift;
    }
    
    @Override
    public Optional<WorkShift> findById(UUID id) {
        WorkShiftEntity entity = entityManager.find(WorkShiftEntity.class, id);
        return Optional.ofNullable(entity)
                .map(WorkShiftMapper::toDomain);
    }
    
    @Override
    public List<WorkShift> findByEmployeeId(EmployeeId employeeId) {
        TypedQuery<WorkShiftEntity> query = entityManager.createQuery(
            "SELECT w FROM WorkShiftEntity w WHERE w.employeeId = :empId ORDER BY w.date, w.startTime",
            WorkShiftEntity.class
        );
        query.setParameter("empId", employeeId.value());
        
        return query.getResultStream()
                .map(WorkShiftMapper::toDomain)
                .toList();
    }
    
    @Override
    public List<WorkShift> findByEmployeeIdAndDateRange(EmployeeId employeeId,
                                                        LocalDate from, LocalDate to) {
        StringBuilder jpql = new StringBuilder(
            "SELECT w FROM WorkShiftEntity w WHERE w.employeeId = :empId"
        );
        if (from != null) {
            jpql.append(" AND w.date >= :from");
        }
        if (to != null) {
            jpql.append(" AND w.date <= :to");
        }
        jpql.append(" ORDER BY w.date, w.startTime");

        TypedQuery<WorkShiftEntity> query = entityManager.createQuery(jpql.toString(), WorkShiftEntity.class);
        query.setParameter("empId", employeeId.value());
        if (from != null) {
            query.setParameter("from", from);
        }
        if (to != null) {
            query.setParameter("to", to);
        }
        
        return query.getResultStream()
                .map(WorkShiftMapper::toDomain)
                .toList();
    }
    
    @Override
    public List<WorkShift> findOverlapping(EmployeeId employeeId, LocalDate date, 
                                            LocalTime startTime, LocalTime endTime) {
        TypedQuery<WorkShiftEntity> query = entityManager.createQuery(
            "SELECT w FROM WorkShiftEntity w WHERE w.employeeId = :empId AND w.date = :date AND " +
            "(w.startTime < :endTime AND w.endTime > :startTime)",
            WorkShiftEntity.class
        );
        query.setParameter("empId", employeeId.value());
        query.setParameter("date", date);
        query.setParameter("startTime", startTime);
        query.setParameter("endTime", endTime);
        
        return query.getResultStream()
                .map(WorkShiftMapper::toDomain)
                .toList();
    }
    
    @Override
    @Transactional
    public void deleteById(UUID id) {
        WorkShiftEntity entity = entityManager.find(WorkShiftEntity.class, id);
        if (entity != null) {
            entityManager.remove(entity);
        }
    }
    
    @Override
    @Transactional
    public void deleteByEmployeeId(EmployeeId employeeId) {
        entityManager.createQuery(
            "DELETE FROM WorkShiftEntity w WHERE w.employeeId = :empId"
        ).setParameter("empId", employeeId.value()).executeUpdate();
    }
}