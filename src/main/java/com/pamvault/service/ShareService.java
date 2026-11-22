package com.pamvault.service;

import com.pamvault.dto.ResourceDtos.ShareRequest;
import com.pamvault.dto.ResourceDtos.ShareResponse;
import com.pamvault.entity.Resource;
import com.pamvault.entity.ResourceShare;
import com.pamvault.entity.User;
import com.pamvault.enums.AuditAction;
import com.pamvault.exception.BadRequestException;
import com.pamvault.exception.NotFoundException;
import com.pamvault.repository.ResourceShareRepository;
import com.pamvault.repository.UserRepository;
import com.pamvault.security.AppUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShareService {

    private final ResourceShareRepository shareRepository;
    private final UserRepository userRepository;
    private final ResourceAccessService accessService;
    private final AccessRequestService accessRequestService;
    private final AuditService auditService;

    @Transactional
    public ShareResponse share(AppUserPrincipal me, Long resourceId, ShareRequest request) {
        Resource resource = accessService.getResource(resourceId);
        accessService.requireOwner(me.getId(), resource);

        User target = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new NotFoundException("User not found: " + request.username()));
        if (target.getId().equals(me.getId())) {
            throw new BadRequestException("You already own this resource");
        }
        if (!target.isActive()) {
            throw new BadRequestException("User " + target.getUsername() + " is disabled");
        }

        ResourceShare share = shareRepository.findByResourceIdAndSharedWithId(resourceId, target.getId())
                .orElseGet(() -> {
                    ResourceShare s = new ResourceShare();
                    s.setResource(resource);
                    s.setSharedWith(target);
                    return s;
                });
        share.setPermission(request.permission());
        shareRepository.save(share);

        auditService.log(me.getUsername(), AuditAction.RESOURCE_SHARED, "RESOURCE", resourceId, resourceId,
                "Shared with " + target.getUsername() + " (" + request.permission() + ")");
        return ShareResponse.from(share);
    }

    @Transactional(readOnly = true)
    public List<ShareResponse> findByResource(AppUserPrincipal me, Long resourceId) {
        Resource resource = accessService.getResource(resourceId);
        accessService.requireOwner(me.getId(), resource);
        return shareRepository.findByResourceId(resourceId).stream().map(ShareResponse::from).toList();
    }

    @Transactional
    public void unshare(AppUserPrincipal me, Long resourceId, Long userId) {
        Resource resource = accessService.getResource(resourceId);
        accessService.requireOwner(me.getId(), resource);

        ResourceShare share = shareRepository.findByResourceIdAndSharedWithId(resourceId, userId)
                .orElseThrow(() -> new NotFoundException("Resource is not shared with this user"));
        String username = share.getSharedWith().getUsername();

        accessRequestService.closeOpenRequests(resourceId, userId, me.getUsername());
        shareRepository.delete(share);

        auditService.log(me.getUsername(), AuditAction.RESOURCE_UNSHARED, "RESOURCE", resourceId, resourceId,
                "Removed access for " + username);
    }
}
