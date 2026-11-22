package com.passvault.controller;

import com.passvault.dto.ResourceDtos.AccountResponse;
import com.passvault.dto.RotationDtos.PasswordHistoryResponse;
import com.passvault.dto.RotationDtos.RotationPolicyRequest;
import com.passvault.dto.RotationDtos.RotationPolicyResponse;
import com.passvault.security.AppUserPrincipal;
import com.passvault.service.RotationService;
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
