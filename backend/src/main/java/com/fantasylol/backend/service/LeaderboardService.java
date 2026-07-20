package com.fantasylol.backend.service;

import com.fantasylol.backend.dto.LeaderboardDto;
import com.fantasylol.backend.entity.Team;
import com.fantasylol.backend.entity.UserScore;
import com.fantasylol.backend.entity.WeeklyStarter;
import com.fantasylol.backend.repository.SeasonWeekView;
import com.fantasylol.backend.repository.TeamRepository;
import com.fantasylol.backend.repository.UserScoreRepository;
import com.fantasylol.backend.repository.WeeklyStarterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Transactional(readOnly = true)
    public LeaderboardDto.Response getLeaderboard(Integer weekNumber, String seasonName, int page, int pageSize) {

        int safePage = Math.max(page, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), MAX_PAGE_SIZE);

        boolean isOverall = weekNumber == null;

        Integer resolvedWeekNumber = weekNumber;
        String resolvedSeasonName = seasonName;

        if (resolvedSeasonName == null) {

            Optional<WeeklyStarter> latest = weeklyStarterRepository.findTopByOrderByLockedAtDesc();

            if (latest.isEmpty()) {
                return LeaderboardDto.Response.builder()
                        .rows(List.of())
                        .hasMore(false)
                        .tallying(true)
                        .weekNumber(null)
                        .seasonName(null)
                        .seasonLabel(null)
                        .build();
            }

            resolvedSeasonName = latest.get().getSeasonName();

            if (!isOverall) {
                resolvedWeekNumber = latest.get().getWeekNumber();
            }

        }

        Pageable pageable = PageRequest.of(safePage - 1, safePageSize);

        Page<UserScore> scorePage = isOverall
                ?
                userScoreRepository.findLatestPerUserBySeasonNameOrderBySeasonalScoreDesc(resolvedSeasonName, pageable)
                :
                userScoreRepository.findByWeekNumberAndSeasonNameOrderByWeeklyScoreDesc(resolvedWeekNumber, resolvedSeasonName, pageable);

        List<UserScore> scores = scorePage.getContent();

        Set<Long> userIds = scores.stream()
                .map(s -> s.getUser().getUserId())
                .collect(Collectors.toSet());

        Map<Long, Team> teamByUserId = teamRepository.findByUserUserIdIn(userIds).stream()
                .collect(Collectors.toMap(t -> t.getUser().getUserId(), t -> t));

        int startRank = (safePage - 1) * safePageSize + 1;

        List<LeaderboardDto.Row> rows = new ArrayList<>();

        for (int i = 0; i < scores.size(); i++) {

            UserScore score = scores.get(i);
            Team team = teamByUserId.get(score.getUser().getUserId());

            rows.add(LeaderboardDto.Row.builder()
                    .rank(startRank + i)
                    .team(team != null ? team.getTeamName() : null)
                    .owner(score.getUser().getUsername())
                    .score(isOverall ? score.getSeasonalScore() : score.getWeeklyScore())
                    .build());

        }

        return LeaderboardDto.Response.builder()
                .rows(rows)
                .hasMore(scorePage.hasNext())
                .tallying(rows.isEmpty())
                .weekNumber(isOverall ? null : resolvedWeekNumber)
                .seasonName(resolvedSeasonName)
                .seasonLabel(formatSeasonLabel(resolvedSeasonName))
                .build();

    }

    private String formatSeasonLabel(String seasonName) {
        if (seasonName == null) return null;
        return seasonName.replace("/", " · ").replace(" Season", "");
    }

    @Transactional(readOnly = true)
    public List<LeaderboardDto.Round> getAvailableRounds() {

        List<SeasonWeekView> pairs = weeklyStarterRepository.findDistinctSeasonWeeks();
        Map<String, List<Integer>> weeksBySeason = new LinkedHashMap<>();

        for (SeasonWeekView p : pairs) {
            weeksBySeason.computeIfAbsent(p.getSeasonName(), k -> new ArrayList<>()).add(p.getWeekNumber());
        }

        return weeksBySeason.entrySet().stream()
                .map(e -> LeaderboardDto.Round.builder()
                        .seasonName(e.getKey())
                        .seasonLabel(formatSeasonLabel(e.getKey()))
                        .weeks(e.getValue())
                        .build())
                .toList();

    }


}
