package com.assessment.employees.infrastructure.persistence.repository;

import com.assessment.employees.domain.model.Employee;
import com.assessment.employees.domain.model.EmployeeId;
import com.assessment.employees.domain.model.Document;
import com.assessment.employees.domain.repository.EmployeeRepository;
import com.assessment.employees.infrastructure.persistence.entity.EmployeeEntity;
import com.assessment.employees.infrastructure.persistence.mapper.EmployeeMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * JPA implementation of EmployeeRepository.
 */
@ApplicationScoped
public class EmployeeJpaRepository implements EmployeeRepository {
    private final EntityManager entityManager;

    @Inject
    public EmployeeJpaRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    
    @Override
    @Transactional
    public Employee save(Employee employee) {
        EmployeeEntity entity = EmployeeMapper.toEntity(employee);
        entityManager.merge(entity);
        return employee;
    }
    
    @Override
    public Optional<Employee> findById(EmployeeId id) {
        EmployeeEntity entity = entityManager.find(EmployeeEntity.class, id.value());
        return Optional.ofNullable(entity)
                .map(EmployeeMapper::toDomain);
    }
    
    @Override
    public Optional<Employee> findByDocumentNumber(String documentNumber) {
        TypedQuery<EmployeeEntity> query = entityManager.createQuery(
            "SELECT e FROM EmployeeEntity e WHERE e.documentNumber = :docNumber",
            EmployeeEntity.class
        );
        query.setParameter("docNumber", documentNumber);
        
        return query.getResultStream()
                .findFirst()
                .map(EmployeeMapper::toDomain);
    }

    @Override
    public Optional<Employee> findByDocument(Document document) {
        TypedQuery<EmployeeEntity> query = entityManager.createQuery(
            "SELECT e FROM EmployeeEntity e WHERE e.documentType = :documentType " +
                "AND e.documentNumber = :documentNumber",
            EmployeeEntity.class
        );
        query.setParameter("documentType", document.type().name());
        query.setParameter("documentNumber", document.number());
        return query.getResultStream().findFirst().map(EmployeeMapper::toDomain);
    }
    
    @Override
    public Optional<Employee> findByEmail(String email) {
        TypedQuery<EmployeeEntity> query = entityManager.createQuery(
            "SELECT e FROM EmployeeEntity e WHERE e.email = :email",
            EmployeeEntity.class
        );
        query.setParameter("email", email);
        
        return query.getResultStream()
                .findFirst()
                .map(EmployeeMapper::toDomain);
    }
    
    @Override
    @Transactional
    public void deleteById(EmployeeId id) {
        EmployeeEntity entity = entityManager.find(EmployeeEntity.class, id.value());
        if (entity != null) {
            entityManager.remove(entity);
        }
    }
    
    @Override
    public boolean existsById(EmployeeId id) {
        return entityManager.find(EmployeeEntity.class, id.value()) != null;
    }
    
    @Override
    public List<Employee> findPage(int offset, int limit) {
        TypedQuery<EmployeeEntity> query = entityManager.createQuery(
            "SELECT e FROM EmployeeEntity e ORDER BY e.id",
            EmployeeEntity.class
        );
        return query.setFirstResult(offset)
                .setMaxResults(limit)
                .getResultStream()
                .map(EmployeeMapper::toDomain)
                .toList();
    }

    @Override
    public long count() {
        return entityManager.createQuery("SELECT COUNT(e) FROM EmployeeEntity e", Long.class)
                .getSingleResult();
    }
}