package com.fantasylol.backend.controller;

import com.fantasylol.backend.dto.SeasonDto;
import com.fantasylol.backend.entity.Season;
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
@RequestMapping("/seasons")
@RequiredArgsConstructor
@Tag(name = "Season", description = "Season management API")
public class SeasonController {

    private final SeasonService seasonService;
    private final MatchScheduleService matchScheduleService;
    private final SeasonWeekService seasonWeekService;
    private final PlayerService playerService;
    private final PlayerPricingService playerPricingService;

    @GetMapping
    @Operation(summary = "[ADMIN] List all registered seasons")
    public ResponseEntity<List<SeasonDto.Response>> listSeasons() {
        return ResponseEntity.ok(seasonService.getAllSeasons());
    }

    @PostMapping("/feature")
    @Operation(summary = "[ADMIN] Mark a season as the one featured in the Info page player rankings")
    public ResponseEntity<String> featureSeason(@RequestParam String seasonName) {
        seasonService.setFeaturedSeason(seasonName);
        return ResponseEntity.ok("랭킹 노출 시즌 설정 완료: " + seasonName);
    }

    @PostMapping("/ranking-min-games")
    @Operation(summary = "[ADMIN] Set the minimum games-played threshold to qualify for that season's player ranking")
    public ResponseEntity<String> setMinGamesForRanking(@RequestParam String seasonName, @RequestParam int minGames) {
        seasonService.setMinGamesForRanking(seasonName, minGames);
        return ResponseEntity.ok("최소 경기수 설정 완료: " + seasonName + " (" + minGames + "경기 이상)");
    }

    @PostMapping("/roster-source")
    @Operation(summary = "[ADMIN] Set which season's participants form the player purchase pool — anyone not in it is auto-marked DEPARTED")
    public ResponseEntity<String> setRosterSourceSeason(@RequestParam String seasonName) {
        playerService.syncStatusForRosterSourceSeason(seasonName);
        seasonService.setRosterSourceSeason(seasonName);
        playerPricingService.calculatePricesForSeason(seasonName);        return ResponseEntity.ok("로스터 구매 기준 시즌 설정 완료: " + seasonName);
    }

    @PostMapping
    @Operation(summary = "[TEST] Register a season (start date auto-derived from its first scheduled match)")
    public ResponseEntity<String> registerSeason(@RequestParam String seasonName) throws Exception {

        Season season = seasonService.registerSeason(seasonName);

        return ResponseEntity.ok("시즌 등록 완료: " + season.getSeasonName()
                + " (1주차 시작일 " + season.getStartDate() + ", 상태 " + season.getStatus() + ")");

    }

    @GetMapping("/detect-new")
    @Operation(summary = "[ADMIN] Detect season names seen in upcoming matches but not yet registered")
    public ResponseEntity<List<String>> detectNewSeasons() throws Exception {

        List<Map<String, String>> upcoming = matchScheduleService.fetchUpcomingMatches();

        return ResponseEntity.ok(seasonService.filterUnregisteredSeasonNames(upcoming));

    }

    @PostMapping("/weeks/lock")
    @Operation(summary = "[TEST] Force-lock a week's starters regardless of match timing (for testing with past data)")
    public ResponseEntity<String> forceLockWeek(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                @RequestParam String seasonName) {
        var week = seasonWeekService.ensureWeekLocked(date, seasonName);
        return ResponseEntity.ok("Week " + week.getWeekNumber() + " 락 완료 (locked at " + week.getStarterLockedAt() + ")");
    }

    @PostMapping("/activate-due")
    @Operation(summary = "[TEST] Manually trigger season activation check — starts due DRAFT seasons only, does not end the current one (normally runs daily at 1am)")
    public ResponseEntity<String> activateDueSeasons() {
        seasonService.activateDueSeasons();
        return ResponseEntity.ok("시즌 활성화 체크 완료");
    }

    @PostMapping("/end")
    @Operation(summary = "[ADMIN] Manually end an ACTIVE season and settle final rankings")
    public ResponseEntity<String> endSeason(@RequestParam String seasonName) {
        seasonService.endSeason(seasonName);
        return ResponseEntity.ok("시즌 종료 완료: " + seasonName);
    }

}
