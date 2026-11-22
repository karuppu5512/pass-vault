package com.pamvault.controller;

import com.pamvault.dto.ResourceDtos.AccountRequest;
import com.pamvault.dto.ResourceDtos.AccountResponse;
import com.pamvault.dto.ResourceDtos.ChangePasswordRequest;
import com.pamvault.dto.ResourceDtos.PasswordResponse;
import com.pamvault.security.AppUserPrincipal;
import com.pamvault.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/resources/{resourceId}/accounts")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse add(@AuthenticationPrincipal AppUserPrincipal me, @PathVariable Long resourceId,
                               @Valid @RequestBody AccountRequest request) {
        return accountService.add(me, resourceId, request);
    }

    @GetMapping("/resources/{resourceId}/accounts")
    public List<AccountResponse> findByResource(@AuthenticationPrincipal AppUserPrincipal me,
                                                @PathVariable Long resourceId) {
        return accountService.findByResource(me, resourceId);
    }

    @GetMapping("/accounts/{id}/password")
    public PasswordResponse viewPassword(@AuthenticationPrincipal AppUserPrincipal me, @PathVariable Long id) {
        return accountService.viewPassword(me, id);
    }

    @PutMapping("/accounts/{id}/password")
    public AccountResponse changePassword(@AuthenticationPrincipal AppUserPrincipal me, @PathVariable Long id,
                                          @Valid @RequestBody(required = false) ChangePasswordRequest request) {
        return accountService.changePassword(me, id, request);
    }

    @DeleteMapping("/accounts/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal AppUserPrincipal me, @PathVariable Long id) {
        accountService.delete(me, id);
    }
}
