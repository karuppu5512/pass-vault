package com.pamvault.service;

import com.pamvault.dto.AuditLogResponse;
import com.pamvault.dto.PageResponse;
import com.pamvault.entity.AuditLog;
import com.pamvault.enums.AuditAction;
import com.pamvault.repository.AuditLogRepository;
import com.pamvault.security.AppUserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
public class AuditService {

    public static final String SYSTEM = "system";

    private final AuditLogRepository auditLogRepository;
    private final ResourceAccessService accessService;

    public void log(String username, AuditAction action, String targetType, Long targetId, Long resourceId,
                    String details) {
        AuditLog entry = new AuditLog();
        entry.setUsername(username);
        entry.setAction(action);
        entry.setTargetType(targetType);
        entry.setTargetId(targetId);
        entry.setResourceId(resourceId);
        entry.setDetails(details);
        entry.setIpAddress(currentIp());
        auditLogRepository.save(entry);
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> findAll(Pageable pageable) {
        return PageResponse.from(auditLogRepository.findAllByOrderByCreatedAtDesc(pageable), AuditLogResponse::from);
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> findByResource(AppUserPrincipal me, Long resourceId, Pageable pageable) {
        accessService.requireOwner(me.getId(), accessService.getResource(resourceId));
        return PageResponse.from(auditLogRepository.findByResourceIdOrderByCreatedAtDesc(resourceId, pageable),
                AuditLogResponse::from);
    }

    private String currentIp() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            HttpServletRequest request = attrs.getRequest();
            String forwarded = request.getHeader("X-Forwarded-For");
            return forwarded != null ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
        }
        return null;
    }
}
