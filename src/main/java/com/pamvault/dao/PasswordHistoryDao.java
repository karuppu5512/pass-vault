package com.pamvault.dao;

import com.pamvault.entity.Account_;
import com.pamvault.entity.PasswordHistory;
import com.pamvault.entity.PasswordHistory_;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PasswordHistoryDao extends BaseDao<PasswordHistory> {

    public PasswordHistoryDao() {
        super(PasswordHistory.class);
    }

    public List<PasswordHistory> findByAccount(Long accountId) {
        HibernateCriteriaBuilder cb = builder();
        CriteriaQuery<PasswordHistory> cq = cb.createQuery(PasswordHistory.class);
        Root<PasswordHistory> history = cq.from(PasswordHistory.class);
        cq.where(cb.equal(history.get(PasswordHistory_.account).get(Account_.id), accountId))
                .orderBy(cb.desc(history.get(PasswordHistory_.changedAt)), cb.desc(history.get(PasswordHistory_.id)));
        return query(cq).list();
    }
}
