package com.passvault.dto;

import com.passvault.entity.AuditLog;
import com.passvault.enums.AuditAction;

import java.time.LocalDateTime;

public record AuditLogResponse(Long id, String username, AuditAction action, String targetType, Long targetId,
                               Long resourceId, String details, String ipAddress, LocalDateTime createdAt) {

    public static AuditLogResponse from(AuditLog log) {
        return new AuditLogResponse(log.getId(), log.getUsername(), log.getAction(), log.getTargetType(),
                log.getTargetId(), log.getResourceId(), log.getDetails(), log.getIpAddress(), log.getCreatedAt());
    }
}
