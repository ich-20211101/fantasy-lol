package com.fantasylol.backend.controller;

import com.fantasylol.backend.dto.LeaderboardDto;
import com.fantasylol.backend.service.LeaderboardService;
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
@RequestMapping("/leaderboard")
@RequiredArgsConstructor
@Tag(name = "Leaderboard", description = "Cross-user ranking API")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @GetMapping
    @Operation(summary = "Get leaderboard for a week/season (defaults to current)")
    public ResponseEntity<LeaderboardDto.Response> getLeaderboard(
            @RequestParam(required = false) Integer weekNumber,
            @RequestParam(required = false) String seasonName,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize
    ) {

        if (weekNumber != null && seasonName == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(leaderboardService.getLeaderboard(weekNumber, seasonName, page, pageSize));

    }

    @GetMapping("/rounds")
    @Operation(summary = "List rounds/weeks with real leaderboard data")
    public ResponseEntity<List<LeaderboardDto.Round>> getLeaderboardRounds() {
        return ResponseEntity.ok(leaderboardService.getAvailableRounds());
    }

}
