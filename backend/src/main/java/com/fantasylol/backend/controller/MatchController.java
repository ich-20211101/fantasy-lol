package com.fantasylol.backend.controller;

import com.fantasylol.backend.dto.RecentMatchDto;
import com.fantasylol.backend.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/matches")
@RequiredArgsConstructor
@Tag(name = "Match", description = "Match management API")
public class MatchController {

    private final MatchSyncService matchSyncService;
    private final PlayerSyncService playerSyncService;
    private final WeeklyStarterService weeklyStarterService;
    private final PlayerPricingService playerPricingService;
    private final MatchResultService matchResultService;

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

    @PostMapping("/sync-season")
    @Operation(summary = "[TEST/INIT] Bulk sync an entire season's matches + player stats, then recalculate player prices from it")
    public ResponseEntity<String> syncSeason(@RequestParam String seasonName) {

        try {
            matchSyncService.syncBySeasonName(seasonName);
            playerPricingService.calculatePricesForSeason(seasonName);
            return ResponseEntity.ok("시즌 전체 동기화 + 가격 산정 완료: " + seasonName);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Sync failed: " + e.getMessage());
        }

    }

        @PostMapping("/players/sync")
    @Operation(summary = "Sync players from Leaguepedia")
    public ResponseEntity<String> syncPlayers(@RequestParam String overviewPage) {

        try {

            int count = playerSyncService.syncPlayers(overviewPage);

            if (count == 0) {
                return ResponseEntity.badRequest().body("동기화된 선수가 없습니다. overviewPage 값을 확인하세요: " + overviewPage);
            }

            return ResponseEntity.ok(count + "명 동기화 완료: " + overviewPage);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Sync failed: " + e.getMessage());
        }

    }

    @PostMapping("/starters/lock")
    @Operation(summary = "[TEST] Lock weekly starters (manual trigger, will later be scheduled)")
    public ResponseEntity<String> lockStarters(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                               @RequestParam String seasonName) {

        int count = weeklyStarterService.lockStartersForDate(date, seasonName);
        return ResponseEntity.ok(count + "개 팀 스타터 락 완료: " + date + ", " + seasonName);

    }

    @GetMapping("/recent-results")
    @Operation(summary = "Fetch the 2 most recently completed matches with series scores (weekly rank-popup)")
    public ResponseEntity<List<RecentMatchDto>> getRecentResults() {
        return ResponseEntity.ok(matchResultService.getRecentResults());
    }

    @GetMapping("/week")
    @Operation(summary = "Fetch this week's (Mon-Sun KST) full match schedule with live scores — 0:0 until a match finishes")
    public ResponseEntity<List<RecentMatchDto>> getWeekMatches() {
        try {
            return ResponseEntity.ok(matchResultService.getWeekMatches());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }

}
