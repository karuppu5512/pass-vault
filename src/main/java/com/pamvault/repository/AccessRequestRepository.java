package com.pamvault.repository;

import com.pamvault.entity.AccessRequest;
import com.pamvault.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AccessRequestRepository extends JpaRepository<AccessRequest, Long> {

    List<AccessRequest> findByRequesterIdOrderByRequestedAtDesc(Long requesterId);

    @Query("select r from AccessRequest r where r.account.resource.owner.id = :ownerId and r.status = :status order by r.requestedAt")
    List<AccessRequest> findForOwner(@Param("ownerId") Long ownerId, @Param("status") RequestStatus status);

    boolean existsByAccountIdAndRequesterIdAndStatusIn(Long accountId, Long requesterId, Collection<RequestStatus> statuses);

    Optional<AccessRequest> findFirstByAccountIdAndRequesterIdAndStatusAndAccessEndTimeAfter(
            Long accountId, Long requesterId, RequestStatus status, LocalDateTime now);

    List<AccessRequest> findByAccountResourceIdAndRequesterIdAndStatusIn(
            Long resourceId, Long requesterId, Collection<RequestStatus> statuses);

    List<AccessRequest> findByStatusAndAccessEndTimeBefore(RequestStatus status, LocalDateTime time);

    List<AccessRequest> findByStatusAndRequestedAtBefore(RequestStatus status, LocalDateTime time);
}
