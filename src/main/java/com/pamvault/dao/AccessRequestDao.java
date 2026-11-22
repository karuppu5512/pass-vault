package com.pamvault.dao;

import com.pamvault.entity.AccessRequest;
import com.pamvault.enums.RequestStatus;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public class AccessRequestDao extends BaseDao<AccessRequest> {

    private static final String WITH_DETAILS = """
            select r from AccessRequest r
            join fetch r.account a
            join fetch a.resource
            join fetch r.requester
            left join fetch r.reviewedBy
            """;

    public AccessRequestDao() {
        super(AccessRequest.class);
    }

    public List<AccessRequest> findByRequester(Long requesterId) {
        return query(WITH_DETAILS + " where r.requester.id = :requesterId order by r.requestedAt desc")
                .setParameter("requesterId", requesterId)
                .list();
    }

    public List<AccessRequest> findForOwner(Long ownerId, RequestStatus status) {
        return query(WITH_DETAILS + " where a.resource.owner.id = :ownerId and r.status = :status order by r.requestedAt")
                .setParameter("ownerId", ownerId)
                .setParameter("status", status)
                .list();
    }

    public boolean existsOpen(Long accountId, Long requesterId, Collection<RequestStatus> statuses) {
        return longQuery("""
                select count(r) from AccessRequest r
                where r.account.id = :accountId and r.requester.id = :requesterId and r.status in (:statuses)
                """)
                .setParameter("accountId", accountId)
                .setParameter("requesterId", requesterId)
                .setParameterList("statuses", statuses)
                .uniqueResult() > 0;
    }

    public Optional<AccessRequest> findActiveGrant(Long accountId, Long requesterId, LocalDateTime now) {
        return query("""
                from AccessRequest r
                where r.account.id = :accountId and r.requester.id = :requesterId
                  and r.status = :status and r.accessEndTime > :now
                order by r.accessEndTime desc
                """)
                .setParameter("accountId", accountId)
                .setParameter("requesterId", requesterId)
                .setParameter("status", RequestStatus.APPROVED)
                .setParameter("now", now)
                .setMaxResults(1)
                .uniqueResultOptional();
    }

    public List<AccessRequest> findOpenOnResource(Long resourceId, Long requesterId,
                                                  Collection<RequestStatus> statuses) {
        return query("""
                from AccessRequest r
                where r.account.resource.id = :resourceId and r.requester.id = :requesterId
                  and r.status in (:statuses)
                """)
                .setParameter("resourceId", resourceId)
                .setParameter("requesterId", requesterId)
                .setParameterList("statuses", statuses)
                .list();
    }

    public List<Long> findExpiredGrantIds(LocalDateTime now) {
        return longQuery("select r.id from AccessRequest r where r.status = :status and r.accessEndTime < :now")
                .setParameter("status", RequestStatus.APPROVED)
                .setParameter("now", now)
                .list();
    }

    public List<Long> findPendingIdsBefore(LocalDateTime cutoff) {
        return longQuery("select r.id from AccessRequest r where r.status = :status and r.requestedAt < :cutoff")
                .setParameter("status", RequestStatus.PENDING)
                .setParameter("cutoff", cutoff)
                .list();
    }
}
