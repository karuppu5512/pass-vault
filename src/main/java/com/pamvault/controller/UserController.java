package com.pamvault.controller;

import com.pamvault.dto.AuthDtos.CreateUserRequest;
import com.pamvault.dto.AuthDtos.UserResponse;
import com.pamvault.security.AppUserPrincipal;
import com.pamvault.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@AuthenticationPrincipal AppUserPrincipal me,
                               @Valid @RequestBody CreateUserRequest request) {
        return userService.create(me, request);
    }

    @GetMapping
    public List<UserResponse> findAll() {
        return userService.findAll();
    }

    @PutMapping("/{id}/disable")
    public UserResponse disable(@AuthenticationPrincipal AppUserPrincipal me, @PathVariable Long id) {
        return userService.setActive(me, id, false);
    }

    @PutMapping("/{id}/enable")
    public UserResponse enable(@AuthenticationPrincipal AppUserPrincipal me, @PathVariable Long id) {
        return userService.setActive(me, id, true);
    }
}
