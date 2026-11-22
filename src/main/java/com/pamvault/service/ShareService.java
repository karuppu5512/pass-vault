package com.pamvault.service;

import com.pamvault.dao.ResourceShareDao;
import com.pamvault.dao.UserDao;
import com.pamvault.dto.ResourceDtos.ShareRequest;
import com.pamvault.dto.ResourceDtos.ShareResponse;
import com.pamvault.entity.Resource;
import com.pamvault.entity.ResourceShare;
import com.pamvault.entity.User;
import com.pamvault.enums.AuditAction;
import com.pamvault.exception.BadRequestException;
import com.pamvault.exception.NotFoundException;
import com.pamvault.security.AppUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShareService {

    private final ResourceShareDao shareDao;
    private final UserDao userDao;
    private final ResourceAccessService accessService;
    private final AccessRequestService accessRequestService;
    private final AuditService auditService;

    @Transactional
    public ShareResponse share(AppUserPrincipal me, Long resourceId, ShareRequest request) {
        Resource resource = accessService.getResource(resourceId);
        accessService.requireOwner(me.getId(), resource);

        User target = userDao.findByUsername(request.username())
                .orElseThrow(() -> new NotFoundException("User not found: " + request.username()));
        if (target.getId().equals(me.getId())) {
            throw new BadRequestException("You already own this resource");
        }
        if (!target.isActive()) {
            throw new BadRequestException("User " + target.getUsername() + " is disabled");
        }

        ResourceShare share = shareDao.find(resourceId, target.getId()).orElse(null);
        if (share == null) {
            share = new ResourceShare();
            share.setResource(resource);
            share.setSharedWith(target);
            share.setPermission(request.permission());
            shareDao.save(share);
        } else {
            share.setPermission(request.permission());
        }

        auditService.log(me.getUsername(), AuditAction.RESOURCE_SHARED, "RESOURCE", resourceId, resourceId,
                "Shared with " + target.getUsername() + " (" + request.permission() + ")");
        return ShareResponse.from(share);
    }

    @Transactional(readOnly = true)
    public List<ShareResponse> findByResource(AppUserPrincipal me, Long resourceId) {
        Resource resource = accessService.getResource(resourceId);
        accessService.requireOwner(me.getId(), resource);
        return shareDao.findByResource(resourceId).stream().map(ShareResponse::from).toList();
    }

    @Transactional
    public void unshare(AppUserPrincipal me, Long resourceId, Long userId) {
        Resource resource = accessService.getResource(resourceId);
        accessService.requireOwner(me.getId(), resource);

        ResourceShare share = shareDao.find(resourceId, userId)
                .orElseThrow(() -> new NotFoundException("Resource is not shared with this user"));
        String username = share.getSharedWith().getUsername();

        accessRequestService.closeOpenRequests(resourceId, userId, me.getUsername());
        shareDao.delete(share);

        auditService.log(me.getUsername(), AuditAction.RESOURCE_UNSHARED, "RESOURCE", resourceId, resourceId,
                "Removed access for " + username);
    }
}
