package com.pamvault.controller;

import com.pamvault.dto.AuditLogResponse;
import com.pamvault.dto.PageResponse;
import com.pamvault.security.AppUserPrincipal;
import com.pamvault.service.AuditService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping("/audit-logs")
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<AuditLogResponse> all(@RequestParam(defaultValue = "0") @Min(0) int page,
                                              @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return auditService.findAll(PageRequest.of(page, size));
    }

    @GetMapping("/resources/{resourceId}/audit-logs")
    public PageResponse<AuditLogResponse> forResource(@AuthenticationPrincipal AppUserPrincipal me,
                                                      @PathVariable Long resourceId,
                                                      @RequestParam(defaultValue = "0") @Min(0) int page,
                                                      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return auditService.findByResource(me, resourceId, PageRequest.of(page, size));
    }
}
