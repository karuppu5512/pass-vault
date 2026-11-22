package com.passvault.config;

import com.passvault.dao.UserDao;
import com.passvault.entity.User;
import com.passvault.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(String... args) {
        if (userDao.count() > 0) {
            return;
        }
        User admin = new User();
        admin.setUsername(adminUsername);
        admin.setEmail(adminUsername + "@passvault.local");
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRole(Role.ADMIN);
        userDao.save(admin);
        log.info("Default admin user '{}' created", adminUsername);
    }
}
