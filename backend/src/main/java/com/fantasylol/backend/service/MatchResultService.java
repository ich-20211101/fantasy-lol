package com.fantasylol.backend.service;

import com.fantasylol.backend.dto.RecentMatchDto;
import com.fantasylol.backend.entity.Match;
import com.fantasylol.backend.repository.MatchRepository;
import com.fantasylol.backend.entity.Season;
import com.fantasylol.backend.repository.PlayerStatRepository;
import com.fantasylol.backend.repository.SeasonRepository;
import com.fantasylol.backend.util.KstTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchResultService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final MatchRepository matchRepository;
    private final PlayerStatRepository playerStatRepository;
    private final MatchScheduleService matchScheduleService;
    private final SeasonRepository seasonRepository;

    @Cacheable(cacheNames = "recentResults")
    @Transactional(readOnly = true)
    public List<RecentMatchDto> getRecentResults() {

        Optional<Match> latest = matchRepository.findFirstByStatusOrderByMatchDateDesc("COMPLETED");

        if (latest.isEmpty()) {
            return List.of();
        }

        LocalDate kstDay = KstTime.toKstDate(latest.get().getMatchDate());
        ZonedDateTime dayStartKst = kstDay.atStartOfDay(KstTime.KST);
        LocalDateTime rangeStartUtc = dayStartKst.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime rangeEndUtc = dayStartKst.plusDays(1).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();

        List<Match> matches = matchRepository.findByStatusAndMatchDateBetweenOrderByMatchDateAsc("COMPLETED", rangeStartUtc, rangeEndUtc);

        List<Long> matchIds = matches.stream().map(Match::getMatchId).toList();

        Map<Long, Map<String, Long>> gameWinsByMatch = playerStatRepository.findGameWinsByMatchIds(matchIds).stream()
                .collect(Collectors.groupingBy(
                        PlayerStatRepository.MatchTeamGameWins::getMatchId,
                        Collectors.toMap(PlayerStatRepository.MatchTeamGameWins::getTeam, PlayerStatRepository.MatchTeamGameWins::getGamesWon)
                ));

        return matches.stream()
                .map(m -> {
                    Map<String, Long> wins = gameWinsByMatch.getOrDefault(m.getMatchId(), Map.of());
                    return RecentMatchDto.builder()
                            .dateTimeUtc(m.getMatchDate().format(FORMATTER))
                            .team1(m.getTeam1())
                            .team2(m.getTeam2())
                            .team1Score(wins.getOrDefault(m.getTeam1(), 0L).intValue())
                            .team2Score(wins.getOrDefault(m.getTeam2(), 0L).intValue())
                            .build();
                })
                .collect(Collectors.toCollection(ArrayList::new));

    }

    @Cacheable(cacheNames = "weekMatches")
    @Transactional(readOnly = true)
    public List<RecentMatchDto> getWeekMatches() throws Exception {

        LocalDate today = KstTime.nowKstDate();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEndExclusive = weekStart.plusDays(7);

        List<String> seasonNames = seasonRepository.findAll().stream()
                .map(Season::getSeasonName)
                .toList();

        List<Map<String, String>> schedule = matchScheduleService.fetchWeekSchedule(weekStart, weekEndExclusive, seasonNames);

        if (schedule.isEmpty()) {
            return List.of();
        }

        ZonedDateTime rangeStartKst = weekStart.atStartOfDay(KstTime.KST);
        ZonedDateTime rangeEndKst = weekEndExclusive.atStartOfDay(KstTime.KST);
        LocalDateTime rangeStartUtc = rangeStartKst.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime rangeEndUtc = rangeEndKst.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();

        List<Match> playedMatches = matchRepository.findByStatusAndMatchDateBetweenOrderByMatchDateAsc("COMPLETED", rangeStartUtc, rangeEndUtc);

        Map<Long, Map<String, Long>> gameWinsByMatch = playerStatRepository.findGameWinsByMatchIds(
                        playedMatches.stream().map(Match::getMatchId).toList()
                ).stream()
                .collect(Collectors.groupingBy(
                        PlayerStatRepository.MatchTeamGameWins::getMatchId,
                        Collectors.toMap(PlayerStatRepository.MatchTeamGameWins::getTeam, PlayerStatRepository.MatchTeamGameWins::getGamesWon)
                ));

        Map<String, Match> playedByKey = playedMatches.stream()
                .collect(Collectors.toMap(
                        m -> matchKey(m.getTeam1(), m.getTeam2(), m.getMatchDate().format(FORMATTER)),
                        m -> m
                ));

        return schedule.stream()
                .map(m -> {
                    Match played = playedByKey.get(matchKey(m.get("team1"), m.get("team2"), m.get("dateTimeUtc")));
                    Map<String, Long> wins = played != null
                            ? gameWinsByMatch.getOrDefault(played.getMatchId(), Map.of())
                            : Map.of();

                    return RecentMatchDto.builder()
                            .dateTimeUtc(m.get("dateTimeUtc"))
                            .team1(m.get("team1"))
                            .team2(m.get("team2"))
                            .team1Score(wins.getOrDefault(m.get("team1"), 0L).intValue())
                            .team2Score(wins.getOrDefault(m.get("team2"), 0L).intValue())
                            .build();
                })
                .collect(Collectors.toCollection(ArrayList::new));

    }

    private String matchKey(String team1, String team2, String dateTimeUtc) {
        return team1 + "|" + team2 + "|" + dateTimeUtc;
    }

}