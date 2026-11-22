package com.passvault.dto;

import com.passvault.entity.Account;
import com.passvault.entity.Resource;
import com.passvault.entity.ResourceShare;
import com.passvault.enums.AccessLevel;
import com.passvault.enums.ResourceType;
import com.passvault.enums.SharePermission;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class ResourceDtos {

    private ResourceDtos() {
    }

    public record ResourceRequest(
            @NotBlank @Size(max = 100) String name,
            @NotNull ResourceType type,
            @Size(max = 255) String host,
            @Size(max = 500) String description) {
    }

    public record ResourceResponse(Long id, String name, ResourceType type, String host, String description,
                                   String owner, AccessLevel myAccess, LocalDateTime createdAt) {

        public static ResourceResponse from(Resource r, AccessLevel level) {
            return new ResourceResponse(r.getId(), r.getName(), r.getType(), r.getHost(), r.getDescription(),
                    r.getOwner().getUsername(), level, r.getCreatedAt());
        }
    }

    public record AccountRequest(
            @NotBlank @Size(max = 100) String loginName,
            @Size(min = 8, max = 128) String password) {
    }

    public record AccountResponse(Long id, Long resourceId, String resourceName, String loginName,
                                  String password, LocalDateTime lastRotatedAt, LocalDateTime createdAt) {

        public static AccountResponse from(Account a) {
            return new AccountResponse(a.getId(), a.getResource().getId(), a.getResource().getName(),
                    a.getLoginName(), "********", a.getLastRotatedAt(), a.getCreatedAt());
        }
    }

    public record ChangePasswordRequest(
            @Size(min = 8, max = 128) String password) {
    }

    public record PasswordResponse(Long accountId, String loginName, String password,
                                   LocalDateTime accessExpiresAt) {
    }

    public record ShareRequest(
            @NotBlank String username,
            @NotNull SharePermission permission) {
    }

    public record ShareResponse(Long id, Long resourceId, Long userId, String username,
                                SharePermission permission, LocalDateTime sharedAt) {

        public static ShareResponse from(ResourceShare s) {
            return new ShareResponse(s.getId(), s.getResource().getId(), s.getSharedWith().getId(),
                    s.getSharedWith().getUsername(), s.getPermission(), s.getSharedAt());
        }
    }
}
