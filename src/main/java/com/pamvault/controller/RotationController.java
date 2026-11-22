package com.pamvault.controller;

import com.pamvault.dto.ResourceDtos.AccountResponse;
import com.pamvault.dto.RotationDtos.PasswordHistoryResponse;
import com.pamvault.dto.RotationDtos.RotationPolicyRequest;
import com.pamvault.dto.RotationDtos.RotationPolicyResponse;
import com.pamvault.security.AppUserPrincipal;
import com.pamvault.service.RotationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts/{accountId}")
@RequiredArgsConstructor
public class RotationController {

    private final RotationService rotationService;

    @PutMapping("/rotation-policy")
    public RotationPolicyResponse save(@AuthenticationPrincipal AppUserPrincipal me, @PathVariable Long accountId,
                                       @Valid @RequestBody RotationPolicyRequest request) {
        return rotationService.savePolicy(me, accountId, request);
    }

    @GetMapping("/rotation-policy")
    public RotationPolicyResponse get(@AuthenticationPrincipal AppUserPrincipal me, @PathVariable Long accountId) {
        return rotationService.getPolicy(me, accountId);
    }

    @PostMapping("/rotate-now")
    public AccountResponse rotateNow(@AuthenticationPrincipal AppUserPrincipal me, @PathVariable Long accountId) {
        return rotationService.rotateNow(me, accountId);
    }

    @GetMapping("/password-history")
    public List<PasswordHistoryResponse> history(@AuthenticationPrincipal AppUserPrincipal me,
                                                 @PathVariable Long accountId) {
        return rotationService.history(me, accountId);
    }
}
