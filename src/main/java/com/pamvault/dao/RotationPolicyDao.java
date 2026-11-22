package com.pamvault.dao;

import com.pamvault.entity.Account_;
import com.pamvault.entity.RotationPolicy;
import com.pamvault.entity.RotationPolicy_;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class RotationPolicyDao extends BaseDao<RotationPolicy> {

    public RotationPolicyDao() {
        super(RotationPolicy.class);
    }

    public Optional<RotationPolicy> findByAccount(Long accountId) {
        HibernateCriteriaBuilder cb = builder();
        CriteriaQuery<RotationPolicy> cq = cb.createQuery(RotationPolicy.class);
        Root<RotationPolicy> policy = cq.from(RotationPolicy.class);
        policy.fetch(RotationPolicy_.updatedBy, JoinType.LEFT);
        cq.where(cb.equal(policy.get(RotationPolicy_.account).get(Account_.id), accountId));
        return query(cq).uniqueResultOptional();
    }

    public List<Long> findDueIds(LocalDateTime now) {
        HibernateCriteriaBuilder cb = builder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<RotationPolicy> policy = cq.from(RotationPolicy.class);
        cq.select(policy.get(RotationPolicy_.id)).where(
                cb.isTrue(policy.get(RotationPolicy_.enabled)),
                cb.lessThanOrEqualTo(policy.get(RotationPolicy_.nextRotationAt), now)
        );
        return query(cq).list();
    }
}
