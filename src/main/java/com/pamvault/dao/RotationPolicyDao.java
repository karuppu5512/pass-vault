package com.pamvault.dao;

import com.pamvault.entity.RotationPolicy;
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
        return query("from RotationPolicy p left join fetch p.updatedBy where p.account.id = :accountId")
                .setParameter("accountId", accountId)
                .uniqueResultOptional();
    }

    public List<Long> findDueIds(LocalDateTime now) {
        return longQuery("select p.id from RotationPolicy p where p.enabled = true and p.nextRotationAt <= :now")
                .setParameter("now", now)
                .list();
    }
}
