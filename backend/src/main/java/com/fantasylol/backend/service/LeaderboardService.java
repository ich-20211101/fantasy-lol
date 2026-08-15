package com.fantasylol.backend.service;

import com.fantasylol.backend.dto.LeaderboardDto;
import com.fantasylol.backend.entity.*;
import com.fantasylol.backend.repository.*;
import com.fantasylol.backend.util.KstTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private static final int MAX_PAGE_SIZE = 50;

    private final UserScoreRepository userScoreRepository;
    private final TeamRepository teamRepository;
    private final WeeklyStarterRepository weeklyStarterRepository;
    private final SeasonRepository seasonRepository;
    private final SeasonWeekRepository seasonWeekRepository;
    private final TeamRosterRepository teamRosterRepository;
    private final WeeklyPlayerScoreRepository weeklyPlayerScoreRepository;
    private final UserRepository userRepository;
    private final SeasonService seasonService;

    @Cacheable(cacheNames = "leaderboard")
    @Transactional(readOnly = true)
    public LeaderboardDto.Response getLeaderboard(Integer weekNumber, String seasonName, int page, int pageSize) {

        int safePage = Math.max(page, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), MAX_PAGE_SIZE);

        boolean isOverall;
        Integer resolvedWeekNumber;
        String resolvedSeasonName;

        if (seasonName == null) {

            Season active = seasonService.getActiveSeason().orElse(null);

            if (active == null) {
                return LeaderboardDto.Response.builder()
                        .rows(List.of())
                        .hasMore(false)
                        .tallying(true)
                        .weekNumber(null)
                        .seasonName(null)
                        .seasonLabel(null)
                        .build();
            }

            resolvedSeasonName = active.getSeasonName();
            resolvedWeekNumber = seasonService.resolveWeekNumber(resolvedSeasonName, KstTime.nowKstDate());
            isOverall = false;

        } else {
            resolvedSeasonName = seasonName;
            resolvedWeekNumber = weekNumber;
            isOverall = weekNumber == null;
        }

        List<Long> participantUserIds;

        if (isOverall) {
            Set<Long> ids = new LinkedHashSet<>(weeklyStarterRepository.findDistinctUserIdsBySeasonName(resolvedSeasonName));
            teamRepository.findBySeasonSeasonName(resolvedSeasonName).forEach(t -> ids.add(t.getUser().getUserId()));
            participantUserIds = new ArrayList<>(ids);
        } else {
            participantUserIds = weeklyStarterRepository.findDistinctUserIdsByWeekNumberAndSeasonName(resolvedWeekNumber, resolvedSeasonName);
        }

        if (participantUserIds.isEmpty()) {
            return LeaderboardDto.Response.builder()
                    .rows(List.of())
                    .hasMore(false)
                    .tallying(true)
                    .weekNumber(isOverall ? null : resolvedWeekNumber)
                    .seasonName(resolvedSeasonName)
                    .seasonLabel(formatSeasonLabel(resolvedSeasonName))
                    .build();
        }

        Map<Long, Double> scoreByUserId = isOverall
                ? userScoreRepository.findLatestPerUserBySeasonName(resolvedSeasonName).stream()
                    .collect(Collectors.toMap(s -> s.getUser().getUserId(), UserScore::getSeasonalScore))
                : userScoreRepository.findByWeekNumberAndSeasonNameOrderByWeeklyScoreDesc(resolvedWeekNumber, resolvedSeasonName).stream()
                    .collect(Collectors.toMap(s -> s.getUser().getUserId(), UserScore::getWeeklyScore));

        Set<Long> userIdSet = new HashSet<>(participantUserIds);

        Map<Long, User> userById = userRepository.findAllById(userIdSet).stream()
                .collect(Collectors.toMap(User::getUserId, u -> u));

        Map<Long, Team> teamByUserId = teamRepository.findByUserUserIdInAndSeasonSeasonName(userIdSet, resolvedSeasonName).stream()
                .collect(Collectors.toMap(t -> t.getUser().getUserId(), t -> t));

        List<LeaderboardDto.Row> allRows = participantUserIds.stream()
                .map(userId -> LeaderboardDto.Row.builder()
                        .userId(userId)
                        .team(Optional.ofNullable(teamByUserId.get(userId)).map(Team::getTeamName).orElse(null))
                        .owner(Optional.ofNullable(userById.get(userId)).map(User::getUsername).orElse(null))
                        .score(scoreByUserId.getOrDefault(userId, 0.0))
                        .build())
                .sorted(Comparator.comparing(LeaderboardDto.Row::getScore, Comparator.reverseOrder()))
                .collect(Collectors.toCollection(ArrayList::new));

        for (int i = 0; i < allRows.size(); i++) {
            allRows.get(i).setRank(i + 1);
        }

        int fromIndex = Math.min((safePage - 1) * safePageSize, allRows.size());
        int toIndex = Math.min(fromIndex + safePageSize, allRows.size());
        List<LeaderboardDto.Row> rows = new ArrayList<>(allRows.subList(fromIndex, toIndex));


        return LeaderboardDto.Response.builder()
                .rows(rows)
                .hasMore(toIndex < allRows.size())
                .tallying(false)
                .weekNumber(isOverall ? null : resolvedWeekNumber)
                .seasonName(resolvedSeasonName)
                .seasonLabel(formatSeasonLabel(resolvedSeasonName))
                .build();

    }

    private String formatSeasonLabel(String seasonName) {
        if (seasonName == null) return null;
        return seasonName.replace("/", " · ").replace(" Season", "");
    }

    @Cacheable(cacheNames = "leaderboardRounds")
    @Transactional(readOnly = true)
    public List<LeaderboardDto.Round> getAvailableRounds() {

        List<Season> seasons = seasonRepository.findByStatusInOrderByStartDateDesc(
                List.of(SeasonStatus.ACTIVE, SeasonStatus.ENDED));

        return seasons.stream()
                .map(season -> {
                    List<Integer> weeks = seasonWeekRepository
                            .findBySeasonSeasonIdOrderByWeekNumberDesc(season.getSeasonId())
                            .stream()
                            .filter(w -> w.getStarterLockedAt() != null)
                            .map(SeasonWeek::getWeekNumber)
                            .collect(Collectors.toCollection(ArrayList::new));

                    return LeaderboardDto.Round.builder()
                            .seasonName(season.getSeasonName())
                            .seasonLabel(formatSeasonLabel(season.getSeasonName()))
                            .weeks(weeks)
                            .build();
                })
                .collect(Collectors.toCollection(ArrayList::new));

    }

    @Cacheable(cacheNames = "leaderboardDetail")
    @Transactional(readOnly = true)
    public LeaderboardDto.DetailResponse getUserDetail(Long userId, Integer weekNumber, String seasonName) {

        String resolvedSeasonName = (seasonName != null)
                ? seasonName
                : weeklyStarterRepository.findTopByOrderByLockedAtDesc()
                .map(ws -> ws.getSeason().getSeasonName())
                .orElseThrow(() -> new IllegalArgumentException("등록된 시즌이 없습니다."));

        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        Season season = seasonRepository.findBySeasonName(resolvedSeasonName)
                .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 시즌입니다: " + resolvedSeasonName));

        Team team = teamRepository.findByUserUserIdAndSeasonSeasonId(userId, season.getSeasonId())
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다."));

        List<TeamRoster> roster = teamRosterRepository.findByTeamTeamId(team.getTeamId());

        Integer rank;
        Double score;
        List<LeaderboardDto.PlayerRow> playerRows;

        if (weekNumber != null) {
            Double weeklyScore = userScoreRepository.findByUserUserIdAndWeekNumberAndSeasonName(userId, weekNumber, resolvedSeasonName)
                    .map(UserScore::getWeeklyScore).orElse(0.0);
            score = weeklyScore;
            rank = (int) userScoreRepository.countByWeekNumberAndSeasonNameAndWeeklyScoreGreaterThan(weekNumber, resolvedSeasonName, weeklyScore) + 1;

            List<WeeklyPlayerScore> weekScores = weeklyPlayerScoreRepository
                    .findByTeamTeamIdAndSeasonSeasonIdAndWeekNumber(team.getTeamId(), season.getSeasonId(), weekNumber);

            Map<Long, WeeklyPlayerScore> byPlayerId = weekScores.stream()
                    .collect(Collectors.toMap(s -> s.getPlayer().getPlayerId(), s -> s));

            playerRows = roster.stream()
                    .map(r -> {
                        Player p = r.getPlayer();
                        WeeklyPlayerScore wps = byPlayerId.get(p.getPlayerId());
                        double curScore = wps != null ? wps.getScore() : 0.0;
                        boolean isStarter = wps != null && Boolean.TRUE.equals(wps.getIsStarter());

                        return LeaderboardDto.PlayerRow.builder()
                                .playerId(p.getPlayerId())
                                .name(p.getPlayerName())
                                .team(r.getPurchaseTeamName() != null ? r.getPurchaseTeamName() : p.getTeamName())
                                .pos(p.getPosition())
                                .isStarter(isStarter)
                                .curScore(curScore)
                                .benchScore(isStarter ? null : curScore)
                                .appliedScore(isStarter ? curScore : null)
                                .build();
                    })
                    .sorted(Comparator.comparing(LeaderboardDto.PlayerRow::isStarter).reversed())
                    .collect(Collectors.toCollection(ArrayList::new));
        } else {
            Double seasonalScore = userScoreRepository.findTopByUserUserIdAndSeasonNameOrderByWeekNumberDesc(userId, resolvedSeasonName)
                    .map(UserScore::getSeasonalScore).orElse(0.0);
            score = seasonalScore;
            rank = (int) userScoreRepository.countLatestPerUserBySeasonNameAndSeasonalScoreGreaterThan(resolvedSeasonName, seasonalScore) + 1;

            List<WeeklyPlayerScore> allScores = weeklyPlayerScoreRepository
                    .findByTeamTeamIdAndSeasonSeasonId(team.getTeamId(), season.getSeasonId());

            Map<Long, Double> totalByPlayer = new HashMap<>();
            Map<Long, Double> appliedByPlayer = new HashMap<>();
            Map<Long, Double> benchByPlayer = new HashMap<>();

            for (WeeklyPlayerScore wps : allScores) {
                Long playerId = wps.getPlayer().getPlayerId();
                totalByPlayer.merge(playerId, wps.getScore(), Double::sum);
                if (Boolean.TRUE.equals(wps.getIsStarter())) {
                    appliedByPlayer.merge(playerId, wps.getScore(), Double::sum);
                } else {
                    benchByPlayer.merge(playerId, wps.getScore(), Double::sum);
                }
            }

            playerRows = roster.stream()
                    .map(r -> {
                        Player p = r.getPlayer();
                        Long playerId = p.getPlayerId();

                        return LeaderboardDto.PlayerRow.builder()
                                .playerId(playerId)
                                .name(p.getPlayerName())
                                .team(r.getPurchaseTeamName() != null ? r.getPurchaseTeamName() : p.getTeamName())
                                .pos(p.getPosition())
                                .isStarter(appliedByPlayer.containsKey(playerId))
                                .curScore(totalByPlayer.getOrDefault(playerId, 0.0))
                                .benchScore(benchByPlayer.get(playerId))
                                .appliedScore(appliedByPlayer.get(playerId))
                                .build();
                    })
                    .sorted(Comparator.comparing(LeaderboardDto.PlayerRow::getCurScore).reversed())
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        return LeaderboardDto.DetailResponse.builder()
                .ownerName(owner.getUsername())
                .teamName(team.getTeamName())
                .rank(rank)
                .score(score)
                .weekNumber(weekNumber)
                .seasonName(resolvedSeasonName)
                .seasonLabel(formatSeasonLabel(resolvedSeasonName))
                .players(playerRows)
                .build();

    }

}
