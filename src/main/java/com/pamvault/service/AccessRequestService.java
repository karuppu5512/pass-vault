package com.pamvault.service;

import com.pamvault.dto.AccessDtos.AccessRequestResponse;
import com.pamvault.dto.AccessDtos.CreateAccessRequest;
import com.pamvault.dto.AccessDtos.ReviewRequest;
import com.pamvault.entity.AccessRequest;
import com.pamvault.entity.Account;
import com.pamvault.enums.AccessLevel;
import com.pamvault.enums.AuditAction;
import com.pamvault.enums.RequestStatus;
import com.pamvault.exception.BadRequestException;
import com.pamvault.exception.ConflictException;
import com.pamvault.exception.ForbiddenException;
import com.pamvault.exception.NotFoundException;
import com.pamvault.repository.AccessRequestRepository;
import com.pamvault.repository.UserRepository;
import com.pamvault.security.AppUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccessRequestService {

    private static final List<RequestStatus> OPEN = List.of(RequestStatus.PENDING, RequestStatus.APPROVED);

    private final AccessRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ResourceAccessService accessService;
    private final RotationService rotationService;
    private final AuditService auditService;

    @Value("${app.access.pending-timeout-hours}")
    private int pendingTimeoutHours;

    @Value("${app.access.rotate-after-checkout}")
    private boolean rotateAfterCheckout;

    @Transactional
    public AccessRequestResponse create(AppUserPrincipal me, Long accountId, CreateAccessRequest request) {
        Account account = accessService.getAccount(accountId);
        AccessLevel level = accessService.requireAccess(me.getId(), account.getResource());

        if (level != AccessLevel.VIEW) {
            throw new BadRequestException("You already have direct access to this password");
        }
        if (requestRepository.existsByAccountIdAndRequesterIdAndStatusIn(accountId, me.getId(), OPEN)) {
            throw new ConflictException("You already have a pending or active request for this account");
        }

        AccessRequest ar = new AccessRequest();
        ar.setAccount(account);
        ar.setRequester(userRepository.getReferenceById(me.getId()));
        ar.setReason(request.reason().trim());
        ar.setDurationMinutes(request.durationMinutes());
        ar.setStatus(RequestStatus.PENDING);
        requestRepository.save(ar);

        audit(me.getUsername(), AuditAction.ACCESS_REQUESTED, ar,
                request.durationMinutes() + " min - " + ar.getReason());
        return AccessRequestResponse.from(ar);
    }

    @Transactional(readOnly = true)
    public List<AccessRequestResponse> findMine(AppUserPrincipal me) {
        return requestRepository.findByRequesterIdOrderByRequestedAtDesc(me.getId()).stream()
                .map(AccessRequestResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AccessRequestResponse> findPendingForOwner(AppUserPrincipal me) {
        return requestRepository.findForOwner(me.getId(), RequestStatus.PENDING).stream()
                .map(AccessRequestResponse::from)
                .toList();
    }

    @Transactional
    public AccessRequestResponse approve(AppUserPrincipal me, Long id, ReviewRequest review) {
        AccessRequest ar = getForOwner(me, id);
        requireStatus(ar, RequestStatus.PENDING);

        if (accessService.accessLevel(ar.getRequester().getId(), ar.getAccount().getResource()).isEmpty()) {
            throw new BadRequestException("Resource is no longer shared with " + ar.getRequester().getUsername());
        }

        LocalDateTime now = LocalDateTime.now();
        ar.setStatus(RequestStatus.APPROVED);
        markReviewed(ar, me, review, now);
        ar.setAccessStartTime(now);
        ar.setAccessEndTime(now.plusMinutes(ar.getDurationMinutes()));

        audit(me.getUsername(), AuditAction.ACCESS_APPROVED, ar, "Access until " + ar.getAccessEndTime());
        return AccessRequestResponse.from(ar);
    }

    @Transactional
    public AccessRequestResponse reject(AppUserPrincipal me, Long id, ReviewRequest review) {
        AccessRequest ar = getForOwner(me, id);
        requireStatus(ar, RequestStatus.PENDING);

        LocalDateTime now = LocalDateTime.now();
        ar.setStatus(RequestStatus.REJECTED);
        markReviewed(ar, me, review, now);
        ar.setClosedAt(now);

        audit(me.getUsername(), AuditAction.ACCESS_REJECTED, ar, ar.getReviewComment());
        return AccessRequestResponse.from(ar);
    }

    @Transactional
    public AccessRequestResponse revoke(AppUserPrincipal me, Long id, ReviewRequest review) {
        AccessRequest ar = getForOwner(me, id);
        requireStatus(ar, RequestStatus.APPROVED);

        close(ar, RequestStatus.REVOKED);
        if (review != null && review.comment() != null) {
            ar.setReviewComment(review.comment());
        }
        audit(me.getUsername(), AuditAction.ACCESS_REVOKED, ar, "Revoked by owner");
        afterCheckout(ar);
        return AccessRequestResponse.from(ar);
    }

    @Transactional
    public AccessRequestResponse cancel(AppUserPrincipal me, Long id) {
        AccessRequest ar = requestRepository.findById(id).orElseThrow(() -> NotFoundException.of("Access request", id));
        if (!ar.getRequester().getId().equals(me.getId())) {
            throw new ForbiddenException("You can only cancel your own requests");
        }
        if (!OPEN.contains(ar.getStatus())) {
            throw new BadRequestException("Request is already " + ar.getStatus());
        }

        boolean wasActive = ar.getStatus() == RequestStatus.APPROVED;
        close(ar, RequestStatus.CANCELLED);
        audit(me.getUsername(), AuditAction.ACCESS_CANCELLED, ar, wasActive ? "Checked in early" : "Cancelled");
        if (wasActive) {
            afterCheckout(ar);
        }
        return AccessRequestResponse.from(ar);
    }

    public void closeOpenRequests(Long resourceId, Long userId, String closedBy) {
        requestRepository.findByAccountResourceIdAndRequesterIdAndStatusIn(resourceId, userId, OPEN).forEach(ar -> {
            boolean wasActive = ar.getStatus() == RequestStatus.APPROVED;
            close(ar, wasActive ? RequestStatus.REVOKED : RequestStatus.CANCELLED);
            audit(closedBy, wasActive ? AuditAction.ACCESS_REVOKED : AuditAction.ACCESS_CANCELLED, ar,
                    "Resource unshared");
            if (wasActive) {
                afterCheckout(ar);
            }
        });
    }

    @Transactional(readOnly = true)
    public List<Long> findFinishedGrantIds() {
        return requestRepository.findByStatusAndAccessEndTimeBefore(RequestStatus.APPROVED, LocalDateTime.now())
                .stream().map(AccessRequest::getId).toList();
    }

    @Transactional(readOnly = true)
    public List<Long> findStalePendingIds() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(pendingTimeoutHours);
        return requestRepository.findByStatusAndRequestedAtBefore(RequestStatus.PENDING, cutoff)
                .stream().map(AccessRequest::getId).toList();
    }

    @Transactional
    public void expire(Long id) {
        AccessRequest ar = requestRepository.findById(id).orElse(null);
        if (ar == null || !OPEN.contains(ar.getStatus())) {
            return;
        }
        boolean wasActive = ar.getStatus() == RequestStatus.APPROVED;
        if (wasActive && ar.getAccessEndTime().isAfter(LocalDateTime.now())) {
            return;
        }

        ar.setStatus(RequestStatus.EXPIRED);
        ar.setClosedAt(LocalDateTime.now());
        audit(AuditService.SYSTEM, AuditAction.ACCESS_EXPIRED, ar,
                wasActive ? "Access window ended" : "No action within " + pendingTimeoutHours + "h");
        if (wasActive) {
            afterCheckout(ar);
        }
    }

    private void afterCheckout(AccessRequest ar) {
        if (rotateAfterCheckout) {
            rotationService.rotateAfterCheckout(ar.getAccount(), ar.getRequester().getUsername());
        }
    }

    private void close(AccessRequest ar, RequestStatus status) {
        LocalDateTime now = LocalDateTime.now();
        if (ar.getStatus() == RequestStatus.APPROVED) {
            ar.setAccessEndTime(now);
        }
        ar.setStatus(status);
        ar.setClosedAt(now);
    }

    private AccessRequest getForOwner(AppUserPrincipal me, Long id) {
        AccessRequest ar = requestRepository.findById(id).orElseThrow(() -> NotFoundException.of("Access request", id));
        accessService.requireOwner(me.getId(), ar.getAccount().getResource());
        return ar;
    }

    private void requireStatus(AccessRequest ar, RequestStatus expected) {
        if (ar.getStatus() != expected) {
            throw new BadRequestException("Request is " + ar.getStatus() + ", expected " + expected);
        }
    }

    private void markReviewed(AccessRequest ar, AppUserPrincipal me, ReviewRequest review, LocalDateTime now) {
        ar.setReviewedBy(userRepository.getReferenceById(me.getId()));
        ar.setReviewedAt(now);
        ar.setReviewComment(review != null ? review.comment() : null);
    }

    private void audit(String username, AuditAction action, AccessRequest ar, String details) {
        auditService.log(username, action, "ACCESS_REQUEST", ar.getId(), ar.getAccount().getResource().getId(), details);
    }
}
