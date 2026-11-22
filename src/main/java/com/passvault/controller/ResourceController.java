package com.passvault.controller;

import com.passvault.dto.ResourceDtos.ResourceRequest;
import com.passvault.dto.ResourceDtos.ResourceResponse;
import com.passvault.dto.ResourceDtos.ShareRequest;
import com.passvault.dto.ResourceDtos.ShareResponse;
import com.passvault.security.AppUserPrincipal;
import com.passvault.service.ResourceService;
import com.passvault.service.ShareService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;
    private final ShareService shareService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResourceResponse create(@AuthenticationPrincipal AppUserPrincipal me,
                                   @Valid @RequestBody ResourceRequest request) {
        return resourceService.create(me, request);
    }

    @GetMapping
    public List<ResourceResponse> findMine(@AuthenticationPrincipal AppUserPrincipal me) {
        return resourceService.findMine(me);
    }

    @GetMapping("/{id}")
    public ResourceResponse findById(@AuthenticationPrincipal AppUserPrincipal me, @PathVariable Long id) {
        return resourceService.findById(me, id);
    }

    @PutMapping("/{id}")
    public ResourceResponse update(@AuthenticationPrincipal AppUserPrincipal me, @PathVariable Long id,
                                   @Valid @RequestBody ResourceRequest request) {
        return resourceService.update(me, id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal AppUserPrincipal me, @PathVariable Long id) {
        resourceService.delete(me, id);
    }

    @PostMapping("/{id}/shares")
    public ShareResponse share(@AuthenticationPrincipal AppUserPrincipal me, @PathVariable Long id,
                               @Valid @RequestBody ShareRequest request) {
        return shareService.share(me, id, request);
    }

    @GetMapping("/{id}/shares")
    public List<ShareResponse> shares(@AuthenticationPrincipal AppUserPrincipal me, @PathVariable Long id) {
        return shareService.findByResource(me, id);
    }

    @DeleteMapping("/{id}/shares/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unshare(@AuthenticationPrincipal AppUserPrincipal me, @PathVariable Long id,
                        @PathVariable Long userId) {
        shareService.unshare(me, id, userId);
    }
}
