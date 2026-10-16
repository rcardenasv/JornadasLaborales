package com.assessment.employees.infrastructure.persistence.repository;

import com.assessment.employees.domain.model.Document;
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
    @Transactional
    public List<WorkShift> saveAll(List<WorkShift> workShifts) {
        workShifts.forEach(shift -> entityManager.merge(WorkShiftMapper.toEntity(shift)));
        return List.copyOf(workShifts);
    }
    
    @Override
    public Optional<WorkShift> findById(UUID id) {
        WorkShiftEntity entity = entityManager.find(WorkShiftEntity.class, id);
        return Optional.ofNullable(entity)
                .map(WorkShiftMapper::toDomain);
    }
    
    @Override
    public List<WorkShift> findByEmployeeDocument(Document employeeDocument) {
        TypedQuery<WorkShiftEntity> query = entityManager.createQuery(
            "SELECT w FROM WorkShiftEntity w WHERE w.employeeDocumentType = :documentType " +
                "AND w.employeeDocumentNumber = :documentNumber ORDER BY w.date, w.startTime",
            WorkShiftEntity.class
        );
        bindDocument(query, employeeDocument);
        
        return query.getResultStream()
                .map(WorkShiftMapper::toDomain)
                .toList();
    }
    
    @Override
    public List<WorkShift> findByEmployeeDocumentAndDateRange(Document employeeDocument,
                                                        LocalDate from, LocalDate to) {
        StringBuilder jpql = new StringBuilder(
            "SELECT w FROM WorkShiftEntity w WHERE w.employeeDocumentType = :documentType " +
                "AND w.employeeDocumentNumber = :documentNumber"
        );
        if (from != null) {
            jpql.append(" AND w.date >= :from");
        }
        if (to != null) {
            jpql.append(" AND w.date <= :to");
        }
        jpql.append(" ORDER BY w.date, w.startTime");

        TypedQuery<WorkShiftEntity> query = entityManager.createQuery(jpql.toString(), WorkShiftEntity.class);
        bindDocument(query, employeeDocument);
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
    @Transactional
    public void deleteById(UUID id) {
        WorkShiftEntity entity = entityManager.find(WorkShiftEntity.class, id);
        if (entity != null) {
            entityManager.remove(entity);
        }
    }
    
    private void bindDocument(TypedQuery<WorkShiftEntity> query, Document document) {
        query.setParameter("documentType", document.type().name());
        query.setParameter("documentNumber", document.number());
    }
}