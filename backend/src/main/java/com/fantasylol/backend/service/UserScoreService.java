package com.fantasylol.backend.service;

import com.fantasylol.backend.dto.UserScoreDto;
import com.fantasylol.backend.entity.*;
import com.fantasylol.backend.repository.*;
import com.fantasylol.backend.util.KstTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserScoreService {

    private final UserScoreRepository userScoreRepository;
    private final WeeklyStarterRepository weeklyStarterRepository;
    private final PlayerStatRepository playerStatRepository;
    private final UserRepository userRepository;
    private final SeasonService seasonService;

    @Transactional
    public void updateScoresForMatch(Match match) {

        List<PlayerStat> stats = playerStatRepository.findByMatchMatchId(match.getMatchId());

        Set<Long> playerIds = stats.stream()
                .map(s -> s.getPlayer().getPlayerId())
                .collect(Collectors.toSet());

        String seasonName = match.getSeasonName();
        int weekNumber = seasonService.resolveWeekNumber(seasonName, KstTime.toKstDate(match.getMatchDate()));

        List<WeeklyStarter> starters = weeklyStarterRepository.findByPlayerPlayerIdInAndWeekNumberAndSeasonName(playerIds, weekNumber, seasonName);

        if (starters.isEmpty()) {
            log.info("No locked starters found for match: {} (week {}, {})", match.getMatchId(), weekNumber, seasonName);
            return;
        }

        Map<Long, Double> scoreByUser = new HashMap<>();
        Map<Long, User> userMap = new HashMap<>();

        for (WeeklyStarter starter : starters) {

            User user = starter.getTeam().getUser();
            Long userId = user.getUserId();
            Long playerId = starter.getPlayer().getPlayerId();

            double score = stats.stream()
                    .filter(s -> s.getPlayer().getPlayerId().equals(playerId))
                    .mapToDouble(PlayerStat::getActualScore)
                    .sum();

            scoreByUser.merge(userId, score, Double::sum);
            userMap.put(userId, user);

        }

        for (Map.Entry<Long, Double> entry : scoreByUser.entrySet()) {

            Long userId = entry.getKey();
            Double delta = entry.getValue();
            User user = userMap.get(userId);

            UserScore userScore = userScoreRepository.findByUserUserIdAndWeekNumberAndSeasonName(userId, weekNumber, seasonName)
                    .orElseGet(() -> UserScore.builder()
                            .user(user)
                            .weekNumber(weekNumber)
                            .seasonName(seasonName)
                            .build());

            userScore.setWeeklyScore(userScore.getWeeklyScore() + delta);

            double currentSeasonalScore = userScoreRepository.findSeasonalScoreByUserIdAndSeasonName(userId, seasonName);
            userScore.setSeasonalScore(currentSeasonalScore + delta);

            userScoreRepository.save(userScore);

            log.info("Updated score for user {} week {}: +{} (seasonal: {})", userId, weekNumber, delta, userScore.getSeasonalScore());

        }

    }

    @Transactional(readOnly = true)
    public UserScoreDto.Response getMyScores(OAuth2User oAuth2User, Integer weekNumber, String seasonName) {

        String email = oAuth2User.getAttribute("email");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String resolvedSeasonName = seasonName;

        if (resolvedSeasonName == null) {
            Optional<WeeklyStarter> latest = weeklyStarterRepository.findTopByOrderByLockedAtDesc();
            if (latest.isEmpty()) {
                return UserScoreDto.Response.builder().rank(null).score(0.0).build();
            }
            resolvedSeasonName = latest.get().getSeasonName();
        }

        if (weekNumber == null) {

            UserScore latest = userScoreRepository
                    .findTopByUserUserIdAndSeasonNameOrderByWeekNumberDesc(user.getUserId(), resolvedSeasonName)
                    .orElse(null);

            if (latest == null) {
                return UserScoreDto.Response.builder().rank(null).score(0.0).build();
            }

            long higherCount = userScoreRepository
                    .countLatestPerUserBySeasonNameAndSeasonalScoreGreaterThan(resolvedSeasonName, latest.getSeasonalScore());

            return UserScoreDto.Response.builder()
                    .rank((int) higherCount + 1)
                    .score(latest.getSeasonalScore())
                    .build();

        }

        UserScore weekScore = userScoreRepository
                .findByUserUserIdAndWeekNumberAndSeasonName(user.getUserId(), weekNumber, resolvedSeasonName)
                .orElse(null);

        if (weekScore == null) {
            return UserScoreDto.Response.builder().rank(null).score(0.0).build();
        }

        long higherCount = userScoreRepository
                .countByWeekNumberAndSeasonNameAndWeeklyScoreGreaterThan(weekNumber, resolvedSeasonName, weekScore.getWeeklyScore());

        return UserScoreDto.Response.builder()
                .rank((int) higherCount + 1)
                .score(weekScore.getWeeklyScore())
                .build();

    }

}
