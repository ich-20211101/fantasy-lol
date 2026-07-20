package com.fantasylol.backend.service;

import com.fantasylol.backend.entity.User;
import com.fantasylol.backend.exception.AdminAuthenticationException;
import com.fantasylol.backend.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private static final String ADMIN_ROLE = "ADMIN";
    private static final String LOGIN_FAILURE_MESSAGE = "이메일 또는 비밀번호가 올바르지 않습니다";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public void login(String email, String rawPassword, HttpServletRequest request, HttpServletResponse response) {

        User admin = userRepository.findByEmail(email)
                .filter(u -> ADMIN_ROLE.equals(u.getRole()))
                .orElseThrow(() -> new AdminAuthenticationException(LOGIN_FAILURE_MESSAGE));

        if (admin.getPassword() == null || !passwordEncoder.matches(rawPassword, admin.getPassword())) {
            throw new AdminAuthenticationException(LOGIN_FAILURE_MESSAGE);
        }

        // 세션 고정 공격 방지 — 인증 성공 시 세션 ID 새로 발급
        request.changeSessionId();

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + ADMIN_ROLE));
        Authentication authentication = new UsernamePasswordAuthenticationToken(admin.getEmail(), null, authorities);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);

        log.info("Admin login: {}", email);

    }

    @Transactional
    public void changePassword(String email, String currentPassword, String newPassword) {

        User admin = userRepository.findByEmail(email)
                .filter(u -> ADMIN_ROLE.equals(u.getRole()))
                .orElseThrow(() -> new AdminAuthenticationException(LOGIN_FAILURE_MESSAGE));

        if (admin.getPassword() == null || !passwordEncoder.matches(currentPassword, admin.getPassword())) {
            throw new AdminAuthenticationException("현재 비밀번호가 올바르지 않습니다");
        }

        admin.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(admin);

        log.info("Admin password changed: {}", email);

    }

}