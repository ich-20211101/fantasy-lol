package com.fantasylol.backend.controller;

import com.fantasylol.backend.dto.ProTeamDto;
import com.fantasylol.backend.service.ProTeamService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pro-teams")
@RequiredArgsConstructor
@Tag(name = "ProTeam", description = "Pro team management API")
public class ProTeamController {

    private final ProTeamService proTeamService;

    @GetMapping
    public ResponseEntity<List<ProTeamDto.Response>> getAllProTeams() {
        return ResponseEntity.ok(proTeamService.getAllProTeams());
    }

    @PostMapping("/sync-from-players")
    public ResponseEntity<String> syncFromPlayers() {
        return ResponseEntity.ok(proTeamService.syncFromPlayers() + "개 팀 새로 등록됨");
    }

    @PutMapping("/{proTeamId}")
    public ResponseEntity<ProTeamDto.Response> updateProTeam(@PathVariable Long proTeamId,
                                                             @RequestBody ProTeamDto.UpdateRequest request) {
        return ResponseEntity.ok(proTeamService.updateProTeam(proTeamId, request));
    }

    @DeleteMapping("/{proTeamId}")
    public ResponseEntity<Void> deleteProTeam(@PathVariable Long proTeamId) {
        proTeamService.deleteProTeam(proTeamId);
        return ResponseEntity.ok().build();
    }

}
