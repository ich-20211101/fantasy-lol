package com.fantasylol.backend.service;

import com.fantasylol.backend.entity.Season;
import com.fantasylol.backend.entity.SeasonStatus;
import com.fantasylol.backend.repository.SeasonRepository;
import com.fantasylol.backend.util.KstTime;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeasonService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SeasonRepository seasonRepository;
    private final LeaguepediaClient leaguepediaClient;
    private final SettlementService settlementService;
    private final ProTeamService proTeamService;

    @Transactional(readOnly = true)
    public Optional<Season> getActiveSeason() {
        return seasonRepository.findByStatus(SeasonStatus.ACTIVE).stream()
                .max(Comparator.comparing(Season::getStartDate));
    }

    @Transactional
    public Season registerSeason(String seasonName) throws Exception {

        if (seasonRepository.existsBySeasonName(seasonName)) {
            throw new IllegalArgumentException("이미 등록된 시즌입니다: " + seasonName);
        }

        LocalDate firstMatchDate = fetchFirstMatchDate(seasonName);
        LocalDate week1Monday = firstMatchDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        Season season = seasonRepository.save(Season.builder()
                .seasonName(seasonName)
                .startDate(week1Monday)
                .status(SeasonStatus.DRAFT)
                .build());

        log.info("Registered season: {} (first match {}, week1 starts {})", seasonName, firstMatchDate, week1Monday);

        return season;

    }

    private LocalDate fetchFirstMatchDate(String seasonName) throws Exception {

        leaguepediaClient.login();

        JsonNode result = leaguepediaClient.cargoQuery(
                "MatchSchedule",
                "DateTime_UTC",
                "OverviewPage = '" + seasonName + "'",
                "DateTime_UTC",
                1
        );

        JsonNode matchList = result.path("cargoquery");

        if (matchList.isEmpty()) {
            throw new IllegalArgumentException("해당 시즌의 경기 일정을 찾을 수 없습니다: " + seasonName);
        }

        String dateTimeStr = matchList.get(0).path("title").path("DateTime UTC").asText();

        return KstTime.toKstDate(LocalDateTime.parse(dateTimeStr, FORMATTER));

    }

    @Transactional(readOnly = true)
    public List<String> filterUnregisteredSeasonNames(List<Map<String, String>> upcomingMatches) {

        Set<String> knownTeams = proTeamService.getKnownTeamNames();

        Set<String> registeredNames = seasonRepository.findAll().stream()
                .map(Season::getSeasonName)
                .collect(Collectors.toSet());

        boolean canFilterByTeam = !knownTeams.isEmpty();

        return upcomingMatches.stream()
                .filter(m -> !canFilterByTeam || knownTeams.contains(m.get("team1")) || knownTeams.contains(m.get("team2")))
                .map(m -> m.get("overviewPage"))
                .filter(name -> name != null && !registeredNames.contains(name))
                .distinct()
                .toList();

    }

    @Transactional(readOnly = true)
    public int resolveWeekNumber(String seasonName, LocalDate date) {

        Season season = seasonRepository.findBySeasonName(seasonName)
                .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 시즌입니다: " + seasonName));

        if (date.isBefore(season.getStartDate())) {
            throw new IllegalArgumentException(
                    "날짜(%s)가 시즌 시작일(%s)보다 이릅니다: %s".formatted(date, season.getStartDate(), seasonName));
        }

        long daysSinceStart = ChronoUnit.DAYS.between(season.getStartDate(), date);

        return (int) (daysSinceStart / 7) + 1;

    }

    @Transactional
    public void activateDueSeasons() {

        List<Season> dueSeasons = seasonRepository.findByStatusAndStartDateLessThanEqual(SeasonStatus.DRAFT, KstTime.nowKstDate());

        for (Season season : dueSeasons) {
            season.setStatus(SeasonStatus.ACTIVE);
            seasonRepository.save(season);
            log.info("Activated season: {} (startDate {})", season.getSeasonName(), season.getStartDate());
        }

    }

    @Transactional
    public void endSeason(String seasonName) {

        Season season = seasonRepository.findBySeasonName(seasonName)
                .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 시즌입니다: " + seasonName));

        if (season.getStatus() != SeasonStatus.ACTIVE) {
            throw new IllegalArgumentException("ACTIVE 상태의 시즌만 종료할 수 있습니다. 현재 상태: " + season.getStatus());
        }

        season.setStatus(SeasonStatus.ENDED);
        season.setEndDate(KstTime.nowKstDate());
        seasonRepository.save(season);
        settlementService.settleSeason(season.getSeasonName());
        log.info("Ended season (manual): {} (endDate {})", season.getSeasonName(), season.getEndDate());

    }

}
