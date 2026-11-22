package com.passvault.dao;

import com.passvault.entity.Account;
import com.passvault.entity.Account_;
import com.passvault.entity.Resource_;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AccountDao extends BaseDao<Account> {

    public AccountDao() {
        super(Account.class);
    }

    public List<Account> findByResource(Long resourceId) {
        HibernateCriteriaBuilder cb = builder();
        CriteriaQuery<Account> cq = cb.createQuery(Account.class);
        Root<Account> account = cq.from(Account.class);
        account.fetch(Account_.resource);
        cq.where(cb.equal(account.get(Account_.resource).get(Resource_.id), resourceId))
                .orderBy(cb.asc(account.get(Account_.loginName)));
        return query(cq).list();
    }

    public boolean existsByLoginName(Long resourceId, String loginName) {
        HibernateCriteriaBuilder cb = builder();
        return count(account -> cb.and(
                cb.equal(account.get(Account_.resource).get(Resource_.id), resourceId),
                cb.equal(cb.lower(account.get(Account_.loginName)), loginName.toLowerCase())
        )) > 0;
    }
}
