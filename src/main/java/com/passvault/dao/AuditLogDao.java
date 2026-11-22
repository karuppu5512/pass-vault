package com.passvault.dao;

import com.passvault.entity.AuditLog;
import com.passvault.entity.AuditLog_;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AuditLogDao extends BaseDao<AuditLog> {

    public AuditLogDao() {
        super(AuditLog.class);
    }

    public List<AuditLog> findPage(Long resourceId, int page, int size) {
        HibernateCriteriaBuilder cb = builder();
        CriteriaQuery<AuditLog> cq = cb.createQuery(AuditLog.class);
        Root<AuditLog> log = cq.from(AuditLog.class);
        if (resourceId != null) {
            cq.where(cb.equal(log.get(AuditLog_.resourceId), resourceId));
        }
        cq.orderBy(cb.desc(log.get(AuditLog_.createdAt)), cb.desc(log.get(AuditLog_.id)));
        return query(cq)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .list();
    }

    public long countAll(Long resourceId) {
        return count(resourceId == null ? null : log -> builder().equal(log.get(AuditLog_.resourceId), resourceId));
    }
}
