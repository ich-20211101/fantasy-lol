package com.fantasylol.backend.service;

import com.fantasylol.backend.entity.Match;
import com.fantasylol.backend.entity.Season;
import com.fantasylol.backend.entity.SeasonWeek;
import com.fantasylol.backend.repository.SeasonWeekRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeasonWeekService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SeasonWeekRepository seasonWeekRepository;
    private final SeasonService seasonService;
    private final MatchScheduleService matchScheduleService;
    private final WeeklyStarterService weeklyStarterService;
    private final SettlementService settlementService;

    @Transactional(readOnly = true)
    public boolean isCurrentWeekLocked() {

        Season activeSeason = seasonService.getActiveSeason().orElse(null);
        if (activeSeason == null) return false;

        int currentWeek = seasonService.resolveWeekNumber(activeSeason.getSeasonName(), LocalDate.now());

        return seasonWeekRepository.findBySeasonSeasonIdAndWeekNumber(activeSeason.getSeasonId(), currentWeek)
                .map(week -> week.getStarterLockedAt() != null)
                .orElse(false);

    }

    @Transactional
    public void lockUpcomingWeekIfDue() throws Exception {

        Season activeSeason = seasonService.getActiveSeason().orElse(null);

        if (activeSeason == null) {
            log.info("활성 시즌 없음, 스킵");
            return;
        }

        List<Map<String, String>> upcoming = matchScheduleService.fetchUpcomingMatches();

        if (upcoming.isEmpty()) {
            log.info("예정된 경기 없음, 스킵");
            return;
        }

        Map<String, String> earliestMatch = upcoming.get(0);
        String seasonName = earliestMatch.get("overviewPage");

        if (!seasonName.equals(activeSeason.getSeasonName())) return;

        LocalDateTime matchTime = LocalDateTime.parse(earliestMatch.get("dateTimeUtc"), FORMATTER);
        LocalDate matchDate = matchTime.toLocalDate();
        int weekNumber = seasonService.resolveWeekNumber(seasonName, matchDate);

        SeasonWeek existing = seasonWeekRepository.findBySeasonSeasonIdAndWeekNumber(activeSeason.getSeasonId(), weekNumber).orElse(null);

        if (existing != null && existing.getStarterLockedAt() != null) return;
        if (LocalDateTime.now().isBefore(matchTime.minusHours(1))) return;

        ensureWeekLocked(matchDate, seasonName);

        log.info("Auto-locked starters for week {} ({}), first match at {}", weekNumber, seasonName, matchTime);

    }

    @Transactional
    public void checkAndFinalizeWeek(Match match) throws Exception {

        Season activeSeason = seasonService.getActiveSeason().orElse(null);
        if (activeSeason == null) return;

        LocalDate matchDate = match.getMatchDate().toLocalDate();
        int weekNumber = seasonService.resolveWeekNumber(match.getSeasonName(), matchDate);

        SeasonWeek seasonWeek = seasonWeekRepository
                .findBySeasonSeasonIdAndWeekNumber(activeSeason.getSeasonId(), weekNumber)
                .orElse(null);
        if (seasonWeek == null || seasonWeek.getFinalizedAt() != null) return;

        boolean weekComplete = matchScheduleService.isLastMatchOfWeek(match.getSeasonName(), seasonWeek.getWeekStartDate(), seasonWeek.getWeekEndDate());
        if (!weekComplete) return;

        seasonWeek.setFinalizedAt(LocalDateTime.now());
        seasonWeekRepository.save(seasonWeek);
        settlementService.settleWeek(match.getSeasonName(), weekNumber);

        log.info("Finalized week {} ({}) — all matches scored", weekNumber, match.getSeasonName());

    }

    @Transactional
    public SeasonWeek ensureWeekLocked(LocalDate date, String seasonName) {

        Season activeSeason = seasonService.getActiveSeason()
                .filter(s -> s.getSeasonName().equals(seasonName))
                .orElseThrow(() -> new IllegalStateException("활성 시즌이 아닙니다: " + seasonName));

        int weekNumber = seasonService.resolveWeekNumber(seasonName, date);

        SeasonWeek seasonWeek = seasonWeekRepository.findBySeasonSeasonIdAndWeekNumber(activeSeason.getSeasonId(), weekNumber)
                .orElseGet(() -> {
                    LocalDate weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                    return seasonWeekRepository.save(SeasonWeek.builder()
                            .season(activeSeason)
                            .weekNumber(weekNumber)
                            .weekStartDate(weekStart)
                            .weekEndDate(weekStart.plusDays(6))
                            .build());
                });

        if (seasonWeek.getStarterLockedAt() == null) {
            weeklyStarterService.lockStartersForDate(date, seasonName);
            seasonWeek.setStarterLockedAt(LocalDateTime.now());
            seasonWeekRepository.save(seasonWeek);
        }

        return seasonWeek;

    }

}
