package com.passvault.service;

import com.passvault.dao.AccessRequestDao;
import com.passvault.dao.AccountDao;
import com.passvault.dto.ResourceDtos.AccountRequest;
import com.passvault.dto.ResourceDtos.AccountResponse;
import com.passvault.dto.ResourceDtos.ChangePasswordRequest;
import com.passvault.dto.ResourceDtos.PasswordResponse;
import com.passvault.entity.AccessRequest;
import com.passvault.entity.Account;
import com.passvault.entity.Resource;
import com.passvault.enums.AccessLevel;
import com.passvault.enums.AuditAction;
import com.passvault.enums.ChangeSource;
import com.passvault.exception.ConflictException;
import com.passvault.exception.ForbiddenException;
import com.passvault.security.AppUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountDao accountDao;
    private final AccessRequestDao accessRequestDao;
    private final ResourceAccessService accessService;
    private final EncryptionService encryptionService;
    private final RotationService rotationService;
    private final AuditService auditService;

    @Transactional
    public AccountResponse add(AppUserPrincipal me, Long resourceId, AccountRequest request) {
        Resource resource = accessService.getResource(resourceId);
        accessService.requireOwner(me.getId(), resource);

        String loginName = request.loginName().trim();
        if (accountDao.existsByLoginName(resourceId, loginName)) {
            throw new ConflictException("Account '" + loginName + "' already exists on this resource");
        }

        Account account = new Account();
        account.setResource(resource);
        account.setLoginName(loginName);
        account.setEncryptedPassword(encryptionService.encrypt(rotationService.resolvePassword(request.password())));
        account.setLastRotatedAt(LocalDateTime.now());
        accountDao.save(account);

        auditService.log(me.getUsername(), AuditAction.ACCOUNT_ADDED, "ACCOUNT", account.getId(), resourceId, loginName);
        return AccountResponse.from(account);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> findByResource(AppUserPrincipal me, Long resourceId) {
        Resource resource = accessService.getResource(resourceId);
        accessService.requireAccess(me.getId(), resource);
        return accountDao.findByResource(resourceId).stream()
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
        accountDao.delete(account);

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
            AccessRequest grant = accessRequestDao.findActiveGrant(accountId, me.getId(), LocalDateTime.now())
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
