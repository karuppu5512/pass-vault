package com.pamvault.service;

import com.pamvault.dto.ResourceDtos.AccountRequest;
import com.pamvault.dto.ResourceDtos.AccountResponse;
import com.pamvault.dto.ResourceDtos.ChangePasswordRequest;
import com.pamvault.dto.ResourceDtos.PasswordResponse;
import com.pamvault.entity.AccessRequest;
import com.pamvault.entity.Account;
import com.pamvault.entity.Resource;
import com.pamvault.enums.AccessLevel;
import com.pamvault.enums.AuditAction;
import com.pamvault.enums.ChangeSource;
import com.pamvault.enums.RequestStatus;
import com.pamvault.exception.ConflictException;
import com.pamvault.exception.ForbiddenException;
import com.pamvault.repository.AccessRequestRepository;
import com.pamvault.repository.AccountRepository;
import com.pamvault.security.AppUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccessRequestRepository accessRequestRepository;
    private final ResourceAccessService accessService;
    private final EncryptionService encryptionService;
    private final RotationService rotationService;
    private final AuditService auditService;

    @Transactional
    public AccountResponse add(AppUserPrincipal me, Long resourceId, AccountRequest request) {
        Resource resource = accessService.getResource(resourceId);
        accessService.requireOwner(me.getId(), resource);

        String loginName = request.loginName().trim();
        if (accountRepository.existsByResourceIdAndLoginNameIgnoreCase(resourceId, loginName)) {
            throw new ConflictException("Account '" + loginName + "' already exists on this resource");
        }

        Account account = new Account();
        account.setResource(resource);
        account.setLoginName(loginName);
        account.setEncryptedPassword(encryptionService.encrypt(rotationService.resolvePassword(request.password())));
        account.setLastRotatedAt(LocalDateTime.now());
        accountRepository.save(account);

        auditService.log(me.getUsername(), AuditAction.ACCOUNT_ADDED, "ACCOUNT", account.getId(), resourceId, loginName);
        return AccountResponse.from(account);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> findByResource(AppUserPrincipal me, Long resourceId) {
        Resource resource = accessService.getResource(resourceId);
        accessService.requireAccess(me.getId(), resource);
        return accountRepository.findByResourceIdOrderByLoginNameAsc(resourceId).stream()
                .map(AccountResponse::from)
                .toList();
    }

    @Transactional
    public AccountResponse changePassword(AppUserPrincipal me, Long accountId, ChangePasswordRequest request) {
        Account account = accessService.getAccount(accountId);
        accessService.requireOwner(me.getId(), account.getResource());

        String password = rotationService.resolvePassword(request == null ? null : request.password());
        rotationService.changePassword(account, password, ChangeSource.USER, me.getUsername(), "Changed by owner");
        return AccountResponse.from(account);
    }

    @Transactional
    public void delete(AppUserPrincipal me, Long accountId) {
        Account account = accessService.getAccount(accountId);
        Long resourceId = account.getResource().getId();
        accessService.requireOwner(me.getId(), account.getResource());
        accountRepository.delete(account);

        auditService.log(me.getUsername(), AuditAction.ACCOUNT_DELETED, "ACCOUNT", accountId, resourceId,
                account.getLoginName());
    }

    @Transactional
    public PasswordResponse viewPassword(AppUserPrincipal me, Long accountId) {
        Account account = accessService.getAccount(accountId);
        AccessLevel level = accessService.requireAccess(me.getId(), account.getResource());

        LocalDateTime expiresAt = null;
        String details = "Direct access as " + level;

        if (level == AccessLevel.VIEW) {
            AccessRequest grant = accessRequestRepository
                    .findFirstByAccountIdAndRequesterIdAndStatusAndAccessEndTimeAfter(
                            accountId, me.getId(), RequestStatus.APPROVED, LocalDateTime.now())
                    .orElseThrow(() -> new ForbiddenException(
                            "You don't have active access to this password. Raise an access request first."));
            expiresAt = grant.getAccessEndTime();
            details = "Via access request #" + grant.getId();
        }

        auditService.log(me.getUsername(), AuditAction.PASSWORD_VIEWED, "ACCOUNT", accountId,
                account.getResource().getId(), details);

        return new PasswordResponse(accountId, account.getLoginName(),
                encryptionService.decrypt(account.getEncryptedPassword()), expiresAt);
    }
}
