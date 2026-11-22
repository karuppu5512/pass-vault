package com.pamvault.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
public abstract class BaseDao<T> {

    @PersistenceContext
    private EntityManager entityManager;

    private final Class<T> entityClass;

    protected BaseDao(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    protected Session session() {
        return entityManager.unwrap(Session.class);
    }

    protected Query<T> query(String hql) {
        return session().createQuery(hql, entityClass);
    }

    protected Query<Long> longQuery(String hql) {
        return session().createQuery(hql, Long.class);
    }

    public T save(T entity) {
        session().persist(entity);
        return entity;
    }

    public Optional<T> findById(Long id) {
        return Optional.ofNullable(session().get(entityClass, id));
    }

    public T getReference(Long id) {
        return session().getReference(entityClass, id);
    }

    public void delete(T entity) {
        session().remove(entity);
    }
}
