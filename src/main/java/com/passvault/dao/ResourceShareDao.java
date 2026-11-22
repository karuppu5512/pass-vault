package com.passvault.dao;

import com.passvault.entity.ResourceShare;
import com.passvault.entity.ResourceShare_;
import com.passvault.entity.Resource_;
import com.passvault.entity.User_;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ResourceShareDao extends BaseDao<ResourceShare> {

    public ResourceShareDao() {
        super(ResourceShare.class);
    }

    public Optional<ResourceShare> find(Long resourceId, Long userId) {
        HibernateCriteriaBuilder cb = builder();
        CriteriaQuery<ResourceShare> cq = cb.createQuery(ResourceShare.class);
        Root<ResourceShare> share = cq.from(ResourceShare.class);
        cq.where(
                cb.equal(share.get(ResourceShare_.resource).get(Resource_.id), resourceId),
                cb.equal(share.get(ResourceShare_.sharedWith).get(User_.id), userId)
        );
        return query(cq).uniqueResultOptional();
    }

    public List<ResourceShare> findByResource(Long resourceId) {
        HibernateCriteriaBuilder cb = builder();
        CriteriaQuery<ResourceShare> cq = cb.createQuery(ResourceShare.class);
        Root<ResourceShare> share = cq.from(ResourceShare.class);
        share.fetch(ResourceShare_.sharedWith);
        cq.where(cb.equal(share.get(ResourceShare_.resource).get(Resource_.id), resourceId))
                .orderBy(cb.asc(share.get(ResourceShare_.sharedAt)));
        return query(cq).list();
    }
}
