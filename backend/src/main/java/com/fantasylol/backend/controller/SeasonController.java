package com.fantasylol.backend.controller;

import com.fantasylol.backend.entity.Season;
import com.fantasylol.backend.service.MatchScheduleService;
import com.fantasylol.backend.service.SeasonService;
import com.fantasylol.backend.service.SeasonWeekService;
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

    @GetMapping
    @Operation(summary = "[ADMIN] List all registered seasons")
    public ResponseEntity<List<Season>> listSeasons() {
        return ResponseEntity.ok(seasonService.getAllSeasons());
    }

    @PostMapping("/feature")
    @Operation(summary = "[ADMIN] Mark a season as the one featured in the Info page player rankings")
    public ResponseEntity<String> featureSeason(@RequestParam String seasonName) {
        seasonService.setFeaturedSeason(seasonName);
        return ResponseEntity.ok("랭킹 노출 시즌 설정 완료: " + seasonName);
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
