package com.pamvault.controller;

import com.pamvault.dto.AuthDtos.LoginRequest;
import com.pamvault.dto.AuthDtos.LoginResponse;
import com.pamvault.dto.AuthDtos.UserResponse;
import com.pamvault.security.AppUserPrincipal;
import com.pamvault.service.AuthService;
import com.pamvault.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal AppUserPrincipal me) {
        return userService.findMe(me);
    }
}
