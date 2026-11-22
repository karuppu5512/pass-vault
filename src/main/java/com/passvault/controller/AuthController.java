package com.passvault.controller;

import com.passvault.dto.AuthDtos.LoginRequest;
import com.passvault.dto.AuthDtos.LoginResponse;
import com.passvault.dto.AuthDtos.UserResponse;
import com.passvault.security.AppUserPrincipal;
import com.passvault.service.AuthService;
import com.passvault.service.UserService;
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
