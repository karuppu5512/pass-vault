package com.passvault.dto;

import com.passvault.entity.User;
import com.passvault.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record LoginRequest(
            @NotBlank String username,
            @NotBlank String password) {
    }

    public record LoginResponse(String token, String tokenType, long expiresInMinutes, String username, Role role) {
    }

    public record CreateUserRequest(
            @NotBlank @Size(min = 3, max = 50)
            @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "only letters, numbers, dot, dash and underscore")
            String username,

            @NotBlank @Email String email,

            @NotBlank @Size(min = 8, max = 64) String password,

            @NotNull Role role) {
    }

    public record UserResponse(Long id, String username, String email, Role role, boolean active,
                               LocalDateTime createdAt) {

        public static UserResponse from(User user) {
            return new UserResponse(user.getId(), user.getUsername(), user.getEmail(),
                    user.getRole(), user.isActive(), user.getCreatedAt());
        }
    }
}
