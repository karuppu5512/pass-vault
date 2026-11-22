package com.pamvault.dao;

import com.pamvault.entity.User;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserDao extends BaseDao<User> {

    public UserDao() {
        super(User.class);
    }

    public Optional<User> findByUsername(String username) {
        return query("from User u where u.username = :username")
                .setParameter("username", username)
                .uniqueResultOptional();
    }

    public boolean existsByUsername(String username) {
        return longQuery("select count(u) from User u where u.username = :username")
                .setParameter("username", username)
                .uniqueResult() > 0;
    }

    public boolean existsByEmail(String email) {
        return longQuery("select count(u) from User u where lower(u.email) = lower(:email)")
                .setParameter("email", email)
                .uniqueResult() > 0;
    }

    public List<User> findAll() {
        return query("from User u order by u.username").list();
    }

    public long count() {
        return longQuery("select count(u) from User u").uniqueResult();
    }
}
