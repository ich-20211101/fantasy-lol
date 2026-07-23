package com.fantasylol.backend.controller;

import com.fantasylol.backend.dto.PlayerRankingDto;
import com.fantasylol.backend.entity.Player;
import com.fantasylol.backend.service.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/players")
@RequiredArgsConstructor
@Tag(name = "Player", description = "Player API")
public class PlayerController {

    private final PlayerService playerService;

    @GetMapping
    @Operation(summary = "Get all players")
    public ResponseEntity<List<Player>> getAllPlayers(@RequestParam(required = false, defaultValue = "false") boolean activeOnly) {
        return ResponseEntity.ok(playerService.getAllPlayers(activeOnly));
    }

    @GetMapping("/rankings")
    @Operation(summary = "Get player rankings for the active season")
    public ResponseEntity<PlayerRankingDto.Response> getPlayerRankings(
            @RequestParam(defaultValue = "ALL") String position,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ResponseEntity.ok(playerService.getPlayerRankings(position, page, pageSize));
    }

}
