package com.fantasylol.backend.service;

import com.fantasylol.backend.dto.UserScoreDto;
import com.fantasylol.backend.entity.*;
import com.fantasylol.backend.repository.PlayerStatRepository;
import com.fantasylol.backend.repository.TeamRosterRepository;
import com.fantasylol.backend.repository.UserRepository;
import com.fantasylol.backend.repository.UserScoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserScoreService {

    private final UserScoreRepository userScoreRepository;
    private final TeamRosterRepository teamRosterRepository;
    private final PlayerStatRepository playerStatRepository;
    private final UserRepository userRepository;

    @Transactional
    public void updateScoresForMatch(Match match) {

        List<PlayerStat> stats = playerStatRepository.findByMatchMatchId(match.getMatchId());

        Set<Long> playerIds = stats.stream()
                .map(s -> s.getPlayer().getPlayerId())
                .collect(Collectors.toSet());

        List<TeamRoster> starters = teamRosterRepository.findByPlayerPlayerIdInAndIsStarterTrue(playerIds);

        if (starters.isEmpty()) {
            log.info("No starters found for match: {}", match.getMatchId());
            return;
        }

        int weekNumber = match.getMatchDate().get(WeekFields.ISO.weekOfWeekBasedYear());
        String seasonName = match.getSeasonName();

        Map<Long, Double> scoreByUser = new HashMap<>();
        Map<Long, User> userMap = new HashMap<>();

        for (TeamRoster roster : starters) {

            User user = roster.getTeam().getUser();
            Long userId = user.getUserId();
            Long playerId = roster.getPlayer().getPlayerId();

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
    public UserScoreDto.Response getMyScores(OAuth2User oAuth2User) {

        String email = oAuth2User.getAttribute("email");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        UserScore latest = userScoreRepository.findTopByUserUserIdOrderByUpdatedAtDesc(user.getUserId())
                .orElse(null);

        if (latest == null) {
            return UserScoreDto.Response.builder()
                    .weeklyScore(0.0)
                    .seasonalScore(0.0)
                    .build();
        }

        int currentWeek = LocalDateTime.now().get(WeekFields.ISO.weekOfWeekBasedYear());

        double weeklyScore = latest.getWeekNumber() == currentWeek ? latest.getWeeklyScore() : 0.0;

        return UserScoreDto.Response.builder()
                .weeklyScore(weeklyScore)
                .seasonalScore(latest.getSeasonalScore())
                .build();

    }

}
