package com.pamvault.dao;

import com.pamvault.entity.Account;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AccountDao extends BaseDao<Account> {

    public AccountDao() {
        super(Account.class);
    }

    public List<Account> findByResource(Long resourceId) {
        return query("from Account a join fetch a.resource where a.resource.id = :resourceId order by a.loginName")
                .setParameter("resourceId", resourceId)
                .list();
    }

    public boolean existsByLoginName(Long resourceId, String loginName) {
        return longQuery("""
                select count(a) from Account a
                where a.resource.id = :resourceId and lower(a.loginName) = lower(:loginName)
                """)
                .setParameter("resourceId", resourceId)
                .setParameter("loginName", loginName)
                .uniqueResult() > 0;
    }
}
