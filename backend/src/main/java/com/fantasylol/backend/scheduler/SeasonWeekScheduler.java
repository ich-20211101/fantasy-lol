package com.fantasylol.backend.scheduler;

import com.fantasylol.backend.service.SeasonWeekService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeasonWeekScheduler {

    private final SeasonWeekService seasonWeekService;

    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void lockUpcomingWeekIfDue() {
        try {
            seasonWeekService.lockUpcomingWeekIfDue();
        } catch (Exception e) {
            log.error("Failed to check/lock upcoming week starters", e);
        }
    }

}
