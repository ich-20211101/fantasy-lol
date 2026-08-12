package com.fantasylol.backend.controller;

import com.fantasylol.backend.dto.PlayerPurchaseDto;
import com.fantasylol.backend.dto.PlayerRankingDto;
import com.fantasylol.backend.entity.Player;
import com.fantasylol.backend.service.PlayerPricingService;
import com.fantasylol.backend.service.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
    private final PlayerPricingService playerPricingService;

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
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) List<Long> playerIds) {
        return ResponseEntity.ok(playerService.getPlayerRankings(position, page, pageSize, playerIds));
    }

    @PostMapping("/pricing/calculate")
    @Operation(summary = "[ADMIN][TEST] Recalculate player prices from a season's final stats (also runs automatically when that season ends)")
    public ResponseEntity<String> calculatePrices(@RequestParam String seasonName) {
        playerPricingService.calculatePricesForSeason(seasonName);
        return ResponseEntity.ok("가격 산정 완료: " + seasonName);
    }

    @PatchMapping("/{playerId}/status")
    @Operation(summary = "[ADMIN] Manually set a player's roster status (CURRENT/DEPARTED)")
    public ResponseEntity<String> updatePlayerStatus(@PathVariable Long playerId, @RequestParam String status) {
        playerService.updatePlayerStatus(playerId, status);
        return ResponseEntity.ok("선수 상태 변경 완료: " + playerId + " -> " + status);
    }

    @GetMapping("/purchase-list")
    @Operation(summary = "Get the player purchase pool (roster-source season's participants, excluding departed) with prices")
    public ResponseEntity<PlayerPurchaseDto.Response> getPurchaseList() {
        return ResponseEntity.ok(playerService.getPurchaseList());
    }

}
