package com.pamvault.service;

import com.pamvault.dao.UserDao;
import com.pamvault.dto.AuthDtos.CreateUserRequest;
import com.pamvault.dto.AuthDtos.UserResponse;
import com.pamvault.entity.User;
import com.pamvault.enums.AuditAction;
import com.pamvault.exception.BadRequestException;
import com.pamvault.exception.ConflictException;
import com.pamvault.exception.NotFoundException;
import com.pamvault.security.AppUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    @Transactional
    public UserResponse create(AppUserPrincipal admin, CreateUserRequest request) {
        if (userDao.existsByUsername(request.username())) {
            throw new ConflictException("Username already taken");
        }
        if (userDao.existsByEmail(request.email())) {
            throw new ConflictException("Email already registered");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        userDao.save(user);

        auditService.log(admin.getUsername(), AuditAction.USER_CREATED, "USER", user.getId(), null,
                "Created " + user.getUsername() + " with role " + user.getRole());
        return UserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userDao.findAll().stream().map(UserResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse findMe(AppUserPrincipal me) {
        return UserResponse.from(getUser(me.getId()));
    }

    @Transactional
    public UserResponse setActive(AppUserPrincipal admin, Long userId, boolean active) {
        if (admin.getId().equals(userId)) {
            throw new BadRequestException("You cannot change your own status");
        }
        User user = getUser(userId);
        user.setActive(active);
        if (!active) {
            auditService.log(admin.getUsername(), AuditAction.USER_DISABLED, "USER", userId, null, user.getUsername());
        }
        return UserResponse.from(user);
    }

    public User getUser(Long id) {
        return userDao.findById(id).orElseThrow(() -> NotFoundException.of("User", id));
    }
}
