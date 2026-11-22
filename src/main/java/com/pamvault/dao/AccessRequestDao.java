package com.pamvault.dao;

import com.pamvault.entity.AccessRequest;
import com.pamvault.entity.AccessRequest_;
import com.pamvault.entity.Account;
import com.pamvault.entity.Account_;
import com.pamvault.entity.Resource_;
import com.pamvault.entity.User_;
import com.pamvault.enums.RequestStatus;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public class AccessRequestDao extends BaseDao<AccessRequest> {

    public AccessRequestDao() {
        super(AccessRequest.class);
    }

    public List<AccessRequest> findByRequester(Long requesterId) {
        HibernateCriteriaBuilder cb = builder();
        CriteriaQuery<AccessRequest> cq = cb.createQuery(AccessRequest.class);
        Root<AccessRequest> request = cq.from(AccessRequest.class);
        fetchDetails(request);
        cq.where(cb.equal(request.get(AccessRequest_.requester).get(User_.id), requesterId))
                .orderBy(cb.desc(request.get(AccessRequest_.requestedAt)));
        return query(cq).list();
    }

    public List<AccessRequest> findForOwner(Long ownerId, RequestStatus status) {
        HibernateCriteriaBuilder cb = builder();
        CriteriaQuery<AccessRequest> cq = cb.createQuery(AccessRequest.class);
        Root<AccessRequest> request = cq.from(AccessRequest.class);
        fetchDetails(request);
        Path<Long> owner = request.get(AccessRequest_.account).get(Account_.resource).get(Resource_.owner).get(User_.id);
        cq.where(cb.equal(owner, ownerId), cb.equal(request.get(AccessRequest_.status), status))
                .orderBy(cb.asc(request.get(AccessRequest_.requestedAt)));
        return query(cq).list();
    }

    public boolean existsOpen(Long accountId, Long requesterId, Collection<RequestStatus> statuses) {
        HibernateCriteriaBuilder cb = builder();
        return count(request -> cb.and(
                cb.equal(request.get(AccessRequest_.account).get(Account_.id), accountId),
                cb.equal(request.get(AccessRequest_.requester).get(User_.id), requesterId),
                request.get(AccessRequest_.status).in(statuses)
        )) > 0;
    }

    public Optional<AccessRequest> findActiveGrant(Long accountId, Long requesterId, LocalDateTime now) {
        HibernateCriteriaBuilder cb = builder();
        CriteriaQuery<AccessRequest> cq = cb.createQuery(AccessRequest.class);
        Root<AccessRequest> request = cq.from(AccessRequest.class);
        cq.where(
                cb.equal(request.get(AccessRequest_.account).get(Account_.id), accountId),
                cb.equal(request.get(AccessRequest_.requester).get(User_.id), requesterId),
                cb.equal(request.get(AccessRequest_.status), RequestStatus.APPROVED),
                cb.greaterThan(request.get(AccessRequest_.accessEndTime), now)
        ).orderBy(cb.desc(request.get(AccessRequest_.accessEndTime)));
        return query(cq).setMaxResults(1).uniqueResultOptional();
    }

    public List<AccessRequest> findOpenOnResource(Long resourceId, Long requesterId,
                                                  Collection<RequestStatus> statuses) {
        HibernateCriteriaBuilder cb = builder();
        CriteriaQuery<AccessRequest> cq = cb.createQuery(AccessRequest.class);
        Root<AccessRequest> request = cq.from(AccessRequest.class);
        cq.where(
                cb.equal(request.get(AccessRequest_.account).get(Account_.resource).get(Resource_.id), resourceId),
                cb.equal(request.get(AccessRequest_.requester).get(User_.id), requesterId),
                request.get(AccessRequest_.status).in(statuses)
        );
        return query(cq).list();
    }

    public List<Long> findExpiredGrantIds(LocalDateTime now) {
        HibernateCriteriaBuilder cb = builder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<AccessRequest> request = cq.from(AccessRequest.class);
        cq.select(request.get(AccessRequest_.id)).where(
                cb.equal(request.get(AccessRequest_.status), RequestStatus.APPROVED),
                cb.lessThan(request.get(AccessRequest_.accessEndTime), now)
        );
        return query(cq).list();
    }

    public List<Long> findPendingIdsBefore(LocalDateTime cutoff) {
        HibernateCriteriaBuilder cb = builder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<AccessRequest> request = cq.from(AccessRequest.class);
        cq.select(request.get(AccessRequest_.id)).where(
                cb.equal(request.get(AccessRequest_.status), RequestStatus.PENDING),
                cb.lessThan(request.get(AccessRequest_.requestedAt), cutoff)
        );
        return query(cq).list();
    }

    private void fetchDetails(Root<AccessRequest> request) {
        Fetch<AccessRequest, Account> account = request.fetch(AccessRequest_.account);
        account.fetch(Account_.resource);
        request.fetch(AccessRequest_.requester);
        request.fetch(AccessRequest_.reviewedBy, JoinType.LEFT);
    }
}
