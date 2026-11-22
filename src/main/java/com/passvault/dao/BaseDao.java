package com.passvault.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.function.Function;

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

    protected HibernateCriteriaBuilder builder() {
        return session().getCriteriaBuilder();
    }

    protected <R> Query<R> query(CriteriaQuery<R> criteria) {
        return session().createQuery(criteria);
    }

    protected long count(Function<Root<T>, Predicate> condition) {
        HibernateCriteriaBuilder cb = builder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<T> root = cq.from(entityClass);
        cq.select(cb.count(root));
        if (condition != null) {
            cq.where(condition.apply(root));
        }
        return query(cq).getSingleResult();
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
