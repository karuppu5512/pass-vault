package com.pamvault.dao;

import com.pamvault.entity.AuditLog;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AuditLogDao extends BaseDao<AuditLog> {

    public AuditLogDao() {
        super(AuditLog.class);
    }

    public List<AuditLog> findPage(int page, int size) {
        return query("from AuditLog l order by l.createdAt desc, l.id desc")
                .setFirstResult(page * size)
                .setMaxResults(size)
                .list();
    }

    public long count() {
        return longQuery("select count(l) from AuditLog l").uniqueResult();
    }

    public List<AuditLog> findPageByResource(Long resourceId, int page, int size) {
        return query("from AuditLog l where l.resourceId = :resourceId order by l.createdAt desc, l.id desc")
                .setParameter("resourceId", resourceId)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .list();
    }

    public long countByResource(Long resourceId) {
        return longQuery("select count(l) from AuditLog l where l.resourceId = :resourceId")
                .setParameter("resourceId", resourceId)
                .uniqueResult();
    }
}
