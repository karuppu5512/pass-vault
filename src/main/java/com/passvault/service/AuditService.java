package com.passvault.service;

import com.passvault.dao.AuditLogDao;
import com.passvault.dto.AuditLogResponse;
import com.passvault.dto.PageResponse;
import com.passvault.entity.AuditLog;
import com.passvault.enums.AuditAction;
import com.passvault.security.AppUserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
public class AuditService {

    public static final String SYSTEM = "system";

    private final AuditLogDao auditLogDao;
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
        auditLogDao.save(entry);
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> findAll(int page, int size) {
        var content = auditLogDao.findPage(null, page, size).stream().map(AuditLogResponse::from).toList();
        return PageResponse.of(content, page, size, auditLogDao.countAll(null));
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> findByResource(AppUserPrincipal me, Long resourceId, int page, int size) {
        accessService.requireOwner(me.getId(), accessService.getResource(resourceId));
        var content = auditLogDao.findPage(resourceId, page, size).stream()
                .map(AuditLogResponse::from)
                .toList();
        return PageResponse.of(content, page, size, auditLogDao.countAll(resourceId));
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
