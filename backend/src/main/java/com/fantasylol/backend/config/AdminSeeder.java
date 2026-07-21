package com.fantasylol.backend.config;

import com.fantasylol.backend.entity.User;
import com.fantasylol.backend.repository.UserRepository;
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
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:}")
    private String adminEmail;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(String... args) {

        if (adminEmail.isBlank() || adminPassword.isBlank()) {
            log.info("ADMIN_EMAIL/ADMIN_PASSWORD 미설정 — 어드민 시딩 스킵");
            return;
        }

        if (userRepository.findByEmail(adminEmail).isPresent()) {
            log.info("어드민 계정 이미 존재: {}", adminEmail);
            return;
        }

        userRepository.save(User.builder()
                .username("admin")
                .email(adminEmail)
                .role("ADMIN")
                .password(passwordEncoder.encode(adminPassword))
                .build());

        log.info("어드민 계정 자동 생성됨: {}", adminEmail);

    }


}
