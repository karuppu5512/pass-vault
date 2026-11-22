package com.pamvault.dao;

import com.pamvault.entity.Resource;
import com.pamvault.entity.ResourceShare;
import com.pamvault.entity.ResourceShare_;
import com.pamvault.entity.Resource_;
import com.pamvault.entity.User_;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ResourceDao extends BaseDao<Resource> {

    public ResourceDao() {
        super(Resource.class);
    }

    public List<Resource> findByOwner(Long ownerId) {
        HibernateCriteriaBuilder cb = builder();
        CriteriaQuery<Resource> cq = cb.createQuery(Resource.class);
        Root<Resource> resource = cq.from(Resource.class);
        resource.fetch(Resource_.owner);
        cq.where(cb.equal(resource.get(Resource_.owner).get(User_.id), ownerId))
                .orderBy(cb.asc(resource.get(Resource_.name)));
        return query(cq).list();
    }

    public List<Resource> findSharedWith(Long userId) {
        HibernateCriteriaBuilder cb = builder();
        CriteriaQuery<Resource> cq = cb.createQuery(Resource.class);
        Root<Resource> resource = cq.from(Resource.class);
        resource.fetch(Resource_.owner);

        Subquery<Long> sharedIds = cq.subquery(Long.class);
        Root<ResourceShare> share = sharedIds.from(ResourceShare.class);
        sharedIds.select(share.get(ResourceShare_.resource).get(Resource_.id))
                .where(cb.equal(share.get(ResourceShare_.sharedWith).get(User_.id), userId));

        cq.where(resource.get(Resource_.id).in(sharedIds))
                .orderBy(cb.asc(resource.get(Resource_.name)));
        return query(cq).list();
    }
}
