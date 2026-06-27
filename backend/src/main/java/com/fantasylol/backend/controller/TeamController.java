package com.fantasylol.backend.controller;

import com.fantasylol.backend.dto.TeamDto;
import com.fantasylol.backend.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/teams")
@RequiredArgsConstructor
@Tag(name = "Team", description = "Fantasy team management API")
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    @Operation(summary = "Create fantasy team")
    public ResponseEntity<TeamDto.Response> createTeam(
            @AuthenticationPrincipal OAuth2User oAuth2User,
            @RequestBody TeamDto.CreateRequest request) {
        return ResponseEntity.ok(teamService.createTeam(oAuth2User, request));
    }

    @GetMapping("/{teamId}")
    @Operation(summary = "Get team")
    public ResponseEntity<TeamDto.Response> getTeam(
            @AuthenticationPrincipal OAuth2User oAuth2User,
            @PathVariable Long teamId) {
        return ResponseEntity.ok(teamService.getTeam(oAuth2User, teamId));
    }

    @PostMapping("/{teamId}/roster")
    @Operation(summary = "Submit roster (8 players, one-time)")
    public ResponseEntity<TeamDto.Response> submitRoster(
            @AuthenticationPrincipal OAuth2User oAuth2User,
            @PathVariable Long teamId,
            @RequestBody TeamDto.RosterSubmitRequest request) {
        return ResponseEntity.ok(teamService.submitRoster(oAuth2User, teamId, request));
    }

    @PutMapping("/{teamId}/starters")
    @Operation(summary = "Update weekly starters (5 players)")
    public ResponseEntity<TeamDto.Response> updateStarters(
            @AuthenticationPrincipal OAuth2User oAuth2User,
            @PathVariable Long teamId,
            @RequestBody TeamDto.StarterUpdateRequest request) {
        return ResponseEntity.ok(teamService.updateStarters(oAuth2User, teamId, request));
    }

}
