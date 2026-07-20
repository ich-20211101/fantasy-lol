package com.fantasylol.backend.scheduler;

import com.fantasylol.backend.service.MatchSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatchSyncScheduler {

    private final MatchSyncService matchSyncService;

    @Scheduled(fixedDelay = 30 * 60 * 1000)
    public void pollRecentMatches() {
        try {
            matchSyncService.syncByDate(LocalDate.now().minusDays(1));
            matchSyncService.syncByDate(LocalDate.now());
        } catch (Exception e) {
            log.error("Failed to poll recent matches", e);
        }
    }

}
