package com.passvault.dao;

import com.passvault.entity.User;
import com.passvault.entity.User_;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserDao extends BaseDao<User> {

    public UserDao() {
        super(User.class);
    }

    public Optional<User> findByUsername(String username) {
        HibernateCriteriaBuilder cb = builder();
        CriteriaQuery<User> cq = cb.createQuery(User.class);
        Root<User> user = cq.from(User.class);
        cq.where(cb.equal(user.get(User_.username), username));
        return query(cq).uniqueResultOptional();
    }

    public boolean existsByUsername(String username) {
        return count(user -> builder().equal(user.get(User_.username), username)) > 0;
    }

    public boolean existsByEmail(String email) {
        return count(user -> builder().equal(builder().lower(user.get(User_.email)), email.toLowerCase())) > 0;
    }

    public List<User> findAll() {
        HibernateCriteriaBuilder cb = builder();
        CriteriaQuery<User> cq = cb.createQuery(User.class);
        Root<User> user = cq.from(User.class);
        cq.orderBy(cb.asc(user.get(User_.username)));
        return query(cq).list();
    }

    public long count() {
        return count(null);
    }
}
