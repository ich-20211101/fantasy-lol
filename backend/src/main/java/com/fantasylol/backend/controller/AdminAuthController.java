package com.fantasylol.backend.controller;

import com.fantasylol.backend.dto.AdminDto;
import com.fantasylol.backend.service.AdminAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody AdminDto.LoginRequest request,
                                      HttpServletRequest httpRequest,
                                      HttpServletResponse httpResponse) {
        adminAuthService.login(request.getEmail(), request.getPassword(), httpRequest, httpResponse);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> me(Authentication authentication) {
        return ResponseEntity.ok(Map.of("email", authentication.getName()));
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(Authentication authentication,
                                               @RequestBody AdminDto.ChangePasswordRequest request) {
        String email = authentication.getName();
        adminAuthService.changePassword(email, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }

}