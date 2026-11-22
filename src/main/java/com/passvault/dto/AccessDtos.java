package com.passvault.dto;

import com.passvault.entity.AccessRequest;
import com.passvault.enums.RequestStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class AccessDtos {

    private AccessDtos() {
    }

    public record CreateAccessRequest(
            @NotBlank @Size(max = 500) String reason,
            @Min(15) @Max(480) int durationMinutes) {
    }

    public record ReviewRequest(@Size(max = 500) String comment) {
    }

    public record AccessRequestResponse(Long id, Long accountId, String loginName, Long resourceId,
                                        String resourceName, String requester, String reason, int durationMinutes,
                                        RequestStatus status, String reviewedBy, String reviewComment,
                                        LocalDateTime requestedAt, LocalDateTime reviewedAt,
                                        LocalDateTime accessStartTime, LocalDateTime accessEndTime) {

        public static AccessRequestResponse from(AccessRequest r) {
            var account = r.getAccount();
            var resource = account.getResource();
            return new AccessRequestResponse(r.getId(), account.getId(), account.getLoginName(),
                    resource.getId(), resource.getName(), r.getRequester().getUsername(), r.getReason(),
                    r.getDurationMinutes(), r.getStatus(),
                    r.getReviewedBy() != null ? r.getReviewedBy().getUsername() : null,
                    r.getReviewComment(), r.getRequestedAt(), r.getReviewedAt(),
                    r.getAccessStartTime(), r.getAccessEndTime());
        }
    }
}
