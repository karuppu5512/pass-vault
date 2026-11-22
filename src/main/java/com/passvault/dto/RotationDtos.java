package com.passvault.dto;

import com.passvault.entity.PasswordHistory;
import com.passvault.entity.RotationPolicy;
import com.passvault.enums.ChangeSource;
import com.passvault.enums.RotationUnit;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public final class RotationDtos {

    private RotationDtos() {
    }

    public record RotationPolicyRequest(
            boolean enabled,
            @Min(1) @Max(365) int intervalValue,
            @NotNull RotationUnit intervalUnit,
            @Min(12) @Max(64) int passwordLength,
            boolean includeSpecialChars) {
    }

    public record RotationPolicyResponse(Long id, Long accountId, boolean enabled, int intervalValue,
                                         RotationUnit intervalUnit, int passwordLength, boolean includeSpecialChars,
                                         LocalDateTime nextRotationAt, LocalDateTime lastRunAt,
                                         String updatedBy, LocalDateTime updatedAt) {

        public static RotationPolicyResponse from(RotationPolicy p) {
            return new RotationPolicyResponse(p.getId(), p.getAccount().getId(), p.isEnabled(),
                    p.getIntervalValue(), p.getIntervalUnit(), p.getPasswordLength(), p.isIncludeSpecialChars(),
                    p.getNextRotationAt(), p.getLastRunAt(),
                    p.getUpdatedBy() != null ? p.getUpdatedBy().getUsername() : null, p.getUpdatedAt());
        }
    }

    public record PasswordHistoryResponse(Long id, ChangeSource source, String changedBy, String reason,
                                          LocalDateTime changedAt) {

        public static PasswordHistoryResponse from(PasswordHistory h) {
            return new PasswordHistoryResponse(h.getId(), h.getSource(), h.getChangedBy(), h.getReason(),
                    h.getChangedAt());
        }
    }
}
