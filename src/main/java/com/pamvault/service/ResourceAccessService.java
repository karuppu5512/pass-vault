package com.pamvault.service;

import com.pamvault.entity.Account;
import com.pamvault.entity.Resource;
import com.pamvault.enums.AccessLevel;
import com.pamvault.enums.SharePermission;
import com.pamvault.exception.ForbiddenException;
import com.pamvault.exception.NotFoundException;
import com.pamvault.repository.AccountRepository;
import com.pamvault.repository.ResourceRepository;
import com.pamvault.repository.ResourceShareRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ResourceAccessService {

    private final ResourceRepository resourceRepository;
    private final AccountRepository accountRepository;
    private final ResourceShareRepository shareRepository;

    public Resource getResource(Long id) {
        return resourceRepository.findById(id).orElseThrow(() -> NotFoundException.of("Resource", id));
    }

    public Account getAccount(Long id) {
        return accountRepository.findById(id).orElseThrow(() -> NotFoundException.of("Account", id));
    }

    public Optional<AccessLevel> accessLevel(Long userId, Resource resource) {
        if (resource.getOwner().getId().equals(userId)) {
            return Optional.of(AccessLevel.OWNER);
        }
        return shareRepository.findByResourceIdAndSharedWithId(resource.getId(), userId)
                .map(share -> share.getPermission() == SharePermission.FULL ? AccessLevel.FULL : AccessLevel.VIEW);
    }

    public AccessLevel requireAccess(Long userId, Resource resource) {
        return accessLevel(userId, resource)
                .orElseThrow(() -> new ForbiddenException("This resource is not shared with you"));
    }

    public void requireOwner(Long userId, Resource resource) {
        if (!resource.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Only the resource owner can do this");
        }
    }
}
