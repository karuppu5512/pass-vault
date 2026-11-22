package com.pamvault.dao;

import com.pamvault.entity.Resource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ResourceDao extends BaseDao<Resource> {

    public ResourceDao() {
        super(Resource.class);
    }

    public List<Resource> findByOwner(Long ownerId) {
        return query("from Resource r join fetch r.owner where r.owner.id = :ownerId order by r.name")
                .setParameter("ownerId", ownerId)
                .list();
    }

    public List<Resource> findSharedWith(Long userId) {
        return query("""
                select r from ResourceShare s
                join s.resource r
                join fetch r.owner
                where s.sharedWith.id = :userId
                order by r.name
                """)
                .setParameter("userId", userId)
                .list();
    }
}
