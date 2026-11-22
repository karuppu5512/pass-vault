package com.pamvault.service;

import com.pamvault.dto.ResourceDtos.ResourceRequest;
import com.pamvault.dto.ResourceDtos.ResourceResponse;
import com.pamvault.entity.Resource;
import com.pamvault.enums.AccessLevel;
import com.pamvault.enums.AuditAction;
import com.pamvault.repository.ResourceRepository;
import com.pamvault.repository.UserRepository;
import com.pamvault.security.AppUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;
    private final ResourceAccessService accessService;
    private final AuditService auditService;

    @Transactional
    public ResourceResponse create(AppUserPrincipal me, ResourceRequest request) {
        Resource resource = new Resource();
        apply(resource, request);
        resource.setOwner(userRepository.getReferenceById(me.getId()));
        resourceRepository.save(resource);

        auditService.log(me.getUsername(), AuditAction.RESOURCE_CREATED, "RESOURCE", resource.getId(),
                resource.getId(), resource.getName());
        return ResourceResponse.from(resource, AccessLevel.OWNER);
    }

    @Transactional(readOnly = true)
    public List<ResourceResponse> findMine(AppUserPrincipal me) {
        List<ResourceResponse> result = new ArrayList<>();
        resourceRepository.findByOwnerIdOrderByNameAsc(me.getId())
                .forEach(r -> result.add(ResourceResponse.from(r, AccessLevel.OWNER)));
        resourceRepository.findSharedWith(me.getId())
                .forEach(r -> result.add(ResourceResponse.from(r, accessService.requireAccess(me.getId(), r))));
        return result;
    }

    @Transactional(readOnly = true)
    public ResourceResponse findById(AppUserPrincipal me, Long id) {
        Resource resource = accessService.getResource(id);
        AccessLevel level = accessService.requireAccess(me.getId(), resource);
        return ResourceResponse.from(resource, level);
    }

    @Transactional
    public ResourceResponse update(AppUserPrincipal me, Long id, ResourceRequest request) {
        Resource resource = accessService.getResource(id);
        accessService.requireOwner(me.getId(), resource);
        apply(resource, request);

        auditService.log(me.getUsername(), AuditAction.RESOURCE_UPDATED, "RESOURCE", id, id, resource.getName());
        return ResourceResponse.from(resource, AccessLevel.OWNER);
    }

    @Transactional
    public void delete(AppUserPrincipal me, Long id) {
        Resource resource = accessService.getResource(id);
        accessService.requireOwner(me.getId(), resource);
        resourceRepository.delete(resource);

        auditService.log(me.getUsername(), AuditAction.RESOURCE_DELETED, "RESOURCE", id, id, resource.getName());
    }

    private void apply(Resource resource, ResourceRequest request) {
        resource.setName(request.name().trim());
        resource.setType(request.type());
        resource.setHost(request.host());
        resource.setDescription(request.description());
    }
}
