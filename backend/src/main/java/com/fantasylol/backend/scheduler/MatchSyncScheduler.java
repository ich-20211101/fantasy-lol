package com.fantasylol.backend.scheduler;

import com.fantasylol.backend.service.MatchSyncService;
import com.fantasylol.backend.util.KstTime;
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
            matchSyncService.syncByDate(KstTime.nowKstDate().minusDays(1));
            matchSyncService.syncByDate(KstTime.nowKstDate());
        } catch (Exception e) {
            log.error("Failed to poll recent matches", e);
        }
    }

}
