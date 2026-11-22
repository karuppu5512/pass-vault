package com.pamvault.service;

import com.pamvault.dto.ResourceDtos.AccountResponse;
import com.pamvault.dto.RotationDtos.PasswordHistoryResponse;
import com.pamvault.dto.RotationDtos.RotationPolicyRequest;
import com.pamvault.dto.RotationDtos.RotationPolicyResponse;
import com.pamvault.entity.Account;
import com.pamvault.entity.PasswordHistory;
import com.pamvault.entity.RotationPolicy;
import com.pamvault.enums.AuditAction;
import com.pamvault.enums.ChangeSource;
import com.pamvault.enums.RotationUnit;
import com.pamvault.exception.BadRequestException;
import com.pamvault.exception.NotFoundException;
import com.pamvault.repository.PasswordHistoryRepository;
import com.pamvault.repository.RotationPolicyRepository;
import com.pamvault.repository.UserRepository;
import com.pamvault.security.AppUserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RotationService {

    private static final int DEFAULT_LENGTH = 16;
    private static final int MIN_INTERVAL_MINUTES = 5;

    private final RotationPolicyRepository policyRepository;
    private final PasswordHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final ResourceAccessService accessService;
    private final EncryptionService encryptionService;
    private final PasswordGenerator passwordGenerator;
    private final AuditService auditService;

    @Transactional
    public RotationPolicyResponse savePolicy(AppUserPrincipal me, Long accountId, RotationPolicyRequest request) {
        Account account = accessService.getAccount(accountId);
        accessService.requireOwner(me.getId(), account.getResource());

        if (request.intervalUnit() == RotationUnit.MINUTES && request.intervalValue() < MIN_INTERVAL_MINUTES) {
            throw new BadRequestException("Minimum rotation interval is " + MIN_INTERVAL_MINUTES + " minutes");
        }

        RotationPolicy policy = policyRepository.findByAccountId(accountId).orElseGet(() -> {
            RotationPolicy p = new RotationPolicy();
            p.setAccount(account);
            return p;
        });
        policy.setEnabled(request.enabled());
        policy.setIntervalValue(request.intervalValue());
        policy.setIntervalUnit(request.intervalUnit());
        policy.setPasswordLength(request.passwordLength());
        policy.setIncludeSpecialChars(request.includeSpecialChars());
        policy.setNextRotationAt(request.enabled() ? nextRun(policy, LocalDateTime.now()) : null);
        policy.setUpdatedBy(userRepository.getReferenceById(me.getId()));
        policyRepository.save(policy);

        auditService.log(me.getUsername(), AuditAction.ROTATION_POLICY_SAVED, "ACCOUNT", accountId,
                account.getResource().getId(), describe(policy));
        return RotationPolicyResponse.from(policy);
    }

    @Transactional(readOnly = true)
    public RotationPolicyResponse getPolicy(AppUserPrincipal me, Long accountId) {
        Account account = accessService.getAccount(accountId);
        accessService.requireOwner(me.getId(), account.getResource());
        return policyRepository.findByAccountId(accountId)
                .map(RotationPolicyResponse::from)
                .orElseThrow(() -> new NotFoundException("No rotation policy configured for this account"));
    }

    @Transactional
    public AccountResponse rotateNow(AppUserPrincipal me, Long accountId) {
        Account account = accessService.getAccount(accountId);
        accessService.requireOwner(me.getId(), account.getResource());

        changePassword(account, generateFor(account), ChangeSource.USER, me.getUsername(), "Manual rotation");
        return AccountResponse.from(account);
    }

    @Transactional(readOnly = true)
    public List<PasswordHistoryResponse> history(AppUserPrincipal me, Long accountId) {
        Account account = accessService.getAccount(accountId);
        accessService.requireOwner(me.getId(), account.getResource());
        return historyRepository.findByAccountIdOrderByChangedAtDesc(accountId).stream()
                .map(PasswordHistoryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Long> findDuePolicyIds() {
        return policyRepository.findByEnabledTrueAndNextRotationAtLessThanEqual(LocalDateTime.now()).stream()
                .map(RotationPolicy::getId)
                .toList();
    }

    @Transactional
    public void runScheduledRotation(Long policyId) {
        RotationPolicy policy = policyRepository.findById(policyId).orElse(null);
        LocalDateTime now = LocalDateTime.now();
        if (policy == null || !policy.isEnabled() || policy.getNextRotationAt().isAfter(now)) {
            return;
        }
        String password = passwordGenerator.generate(policy.getPasswordLength(), policy.isIncludeSpecialChars());
        changePassword(policy.getAccount(), password, ChangeSource.SCHEDULER, AuditService.SYSTEM, "Scheduled rotation");

        policy.setLastRunAt(now);
        policy.setNextRotationAt(nextRun(policy, now));
    }

    @Transactional
    public void rotateAfterCheckout(Account account, String requester) {
        changePassword(account, generateFor(account), ChangeSource.SCHEDULER, AuditService.SYSTEM,
                "Access ended for " + requester);
    }

    public void changePassword(Account account, String newPassword, ChangeSource source, String changedBy,
                               String reason) {
        PasswordHistory old = new PasswordHistory();
        old.setAccount(account);
        old.setEncryptedPassword(account.getEncryptedPassword());
        old.setSource(source);
        old.setChangedBy(changedBy);
        old.setReason(reason);
        historyRepository.save(old);

        account.setEncryptedPassword(encryptionService.encrypt(newPassword));
        account.setLastRotatedAt(LocalDateTime.now());

        AuditAction action = source == ChangeSource.SCHEDULER ? AuditAction.PASSWORD_ROTATED : AuditAction.PASSWORD_CHANGED;
        auditService.log(changedBy, action, "ACCOUNT", account.getId(), account.getResource().getId(), reason);
        log.info("Password changed for account {} ({})", account.getId(), reason);
    }

    public String generateFor(Account account) {
        return policyRepository.findByAccountId(account.getId())
                .map(p -> passwordGenerator.generate(p.getPasswordLength(), p.isIncludeSpecialChars()))
                .orElseGet(() -> passwordGenerator.generate(DEFAULT_LENGTH, true));
    }

    public String resolvePassword(String requested) {
        return StringUtils.hasText(requested) ? requested : passwordGenerator.generate(DEFAULT_LENGTH, true);
    }

    private LocalDateTime nextRun(RotationPolicy policy, LocalDateTime from) {
        return from.plus(policy.getIntervalValue(), policy.getIntervalUnit().toChronoUnit());
    }

    private String describe(RotationPolicy p) {
        return (p.isEnabled() ? "Enabled" : "Disabled") + ", every " + p.getIntervalValue() + " "
                + p.getIntervalUnit().name().toLowerCase();
    }
}
