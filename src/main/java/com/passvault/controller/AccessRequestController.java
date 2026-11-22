package com.passvault.controller;

import com.passvault.dto.AccessDtos.AccessRequestResponse;
import com.passvault.dto.AccessDtos.CreateAccessRequest;
import com.passvault.dto.AccessDtos.ReviewRequest;
import com.passvault.security.AppUserPrincipal;
import com.passvault.service.AccessRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AccessRequestController {

    private final AccessRequestService accessRequestService;

    @PostMapping("/accounts/{accountId}/access-requests")
    @ResponseStatus(HttpStatus.CREATED)
    public AccessRequestResponse create(@AuthenticationPrincipal AppUserPrincipal me, @PathVariable Long accountId,
                                        @Valid @RequestBody CreateAccessRequest request) {
        return accessRequestService.create(me, accountId, request);
    }

    @GetMapping("/access-requests/my")
    public List<AccessRequestResponse> mine(@AuthenticationPrincipal AppUserPrincipal me) {
        return accessRequestService.findMine(me);
    }

    @GetMapping("/access-requests/pending")
    public List<AccessRequestResponse> pending(@AuthenticationPrincipal AppUserPrincipal me) {
        return accessRequestService.findPendingForOwner(me);
    }

    @PutMapping("/access-requests/{id}/approve")
    public AccessRequestResponse approve(@AuthenticationPrincipal AppUserPrincipal me, @PathVariable Long id,
                                         @Valid @RequestBody(required = false) ReviewRequest review) {
        return accessRequestService.approve(me, id, review);
    }

    @PutMapping("/access-requests/{id}/reject")
    public AccessRequestResponse reject(@AuthenticationPrincipal AppUserPrincipal me, @PathVariable Long id,
                                        @Valid @RequestBody(required = false) ReviewRequest review) {
        return accessRequestService.reject(me, id, review);
    }

    @PutMapping("/access-requests/{id}/revoke")
    public AccessRequestResponse revoke(@AuthenticationPrincipal AppUserPrincipal me, @PathVariable Long id,
                                        @Valid @RequestBody(required = false) ReviewRequest review) {
        return accessRequestService.revoke(me, id, review);
    }

    @PutMapping("/access-requests/{id}/cancel")
    public AccessRequestResponse cancel(@AuthenticationPrincipal AppUserPrincipal me, @PathVariable Long id) {
        return accessRequestService.cancel(me, id);
    }
}
