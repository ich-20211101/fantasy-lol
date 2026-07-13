package com.fantasylol.backend.controller;

import com.fantasylol.backend.dto.UserDto;
import com.fantasylol.backend.dto.UserScoreDto;
import com.fantasylol.backend.repository.UserRepository;
import com.fantasylol.backend.service.LeaguepediaClient;
import com.fantasylol.backend.service.UserScoreService;
import com.fantasylol.backend.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "User management API")
public class UserController {

    private final UserService userService;
    private final LeaguepediaClient leaguepediaClient;
    private final UserScoreService userScoreService;

    @PostMapping
    @Operation(summary = "Creat User")
    public ResponseEntity<UserDto.Response> createUser(@RequestBody UserDto.Request request) {
        return ResponseEntity.ok(userService.createUser(request));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user")
    public ResponseEntity<UserDto.Response> getCurrentUser(@AuthenticationPrincipal OAuth2User oAuth2User) {

        if (oAuth2User == null) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(userService.getCurrentUser(oAuth2User));

    }

    @PostMapping("/logout")
    @Operation(summary = "Logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {

        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        return ResponseEntity.ok().build();

    }

    @GetMapping("/me/scores")
    public ResponseEntity<UserScoreDto.Response> getMyScores(@AuthenticationPrincipal OAuth2User oAuth2User) {
        return ResponseEntity.ok(userScoreService.getMyScores(oAuth2User));
    }

    @PatchMapping("/me/nickname")
    @Operation(summary = "Update nickname")
    public ResponseEntity<UserDto.Response> updateNickname(@AuthenticationPrincipal OAuth2User oAuth2User,
                                                           @RequestBody UserDto.NicknameUpdateRequest request) {
        return ResponseEntity.ok(userService.updateNickname(oAuth2User, request));
    }

    @DeleteMapping("/me")
    @Operation(summary = "Withdraw (delete account)")
    public ResponseEntity<Void> withdraw(@AuthenticationPrincipal OAuth2User oAuth2User,
                                         @RequestBody UserDto.WithdrawRequest request,
                                         HttpServletRequest httpRequest) {

        userService.withdraw(oAuth2User, request);

        SecurityContextHolder.clearContext();
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        return ResponseEntity.ok().build();

    }

}
