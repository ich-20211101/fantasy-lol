package com.fantasylol.backend.controller;

import com.fantasylol.backend.repository.MatchRepository;
import com.fantasylol.backend.service.MatchSyncService;
import com.fantasylol.backend.service.PlayerSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/matches")
@RequiredArgsConstructor
@Tag(name = "Match", description = "Match management API")
public class MatchController {

    private final MatchSyncService matchSyncService;
    private final PlayerSyncService playerSyncService;

    @PostMapping("/sync")
    @Operation(summary = "Sync match data by date")
    public ResponseEntity<String> syncMatches(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate date) {

        try {
            matchSyncService.syncByDate(date);
            return ResponseEntity.ok("Sync completed for date: " + date);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Sync failed: " + e.getMessage());
        }

    }

    @PostMapping("/players/sync")
    @Operation(summary = "Sync players from Leaguepedia")
    public ResponseEntity<String> syncPlayers(@RequestParam String overviewPage) {

        try {
            playerSyncService.syncPlayers(overviewPage);
            return ResponseEntity.ok("Player sync completed: " + overviewPage);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Sync failed: " + e.getMessage());
        }

    }

}
