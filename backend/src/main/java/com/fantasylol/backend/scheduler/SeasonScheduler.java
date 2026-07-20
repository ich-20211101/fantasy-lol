package com.fantasylol.backend.scheduler;

import com.fantasylol.backend.service.SeasonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeasonScheduler {

    private final SeasonService seasonService;

    @Scheduled(cron = "0 0 1 * * *")
    public void activateDueSeasons() {
        seasonService.activateDueSeasons();
    }

}
