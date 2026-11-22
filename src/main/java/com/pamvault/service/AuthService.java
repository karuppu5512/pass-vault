package com.pamvault.service;

import com.pamvault.dto.AuthDtos.LoginRequest;
import com.pamvault.dto.AuthDtos.LoginResponse;
import com.pamvault.enums.AuditAction;
import com.pamvault.security.AppUserPrincipal;
import com.pamvault.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuditService auditService;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        AppUserPrincipal user = (AppUserPrincipal) auth.getPrincipal();
        auditService.log(user.getUsername(), AuditAction.LOGIN, "USER", user.getId(), null, null);

        return new LoginResponse(jwtService.generateToken(user), "Bearer", jwtService.getExpiryMinutes(),
                user.getUsername(), user.getRole());
    }
}
