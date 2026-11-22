package com.pamvault.dao;

import com.pamvault.entity.ResourceShare;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ResourceShareDao extends BaseDao<ResourceShare> {

    public ResourceShareDao() {
        super(ResourceShare.class);
    }

    public Optional<ResourceShare> find(Long resourceId, Long userId) {
        return query("from ResourceShare s where s.resource.id = :resourceId and s.sharedWith.id = :userId")
                .setParameter("resourceId", resourceId)
                .setParameter("userId", userId)
                .uniqueResultOptional();
    }

    public List<ResourceShare> findByResource(Long resourceId) {
        return query("from ResourceShare s join fetch s.sharedWith where s.resource.id = :resourceId order by s.sharedAt")
                .setParameter("resourceId", resourceId)
                .list();
    }
}
