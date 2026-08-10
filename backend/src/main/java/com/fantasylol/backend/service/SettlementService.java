package com.fantasylol.backend.service;

import com.fantasylol.backend.entity.Season;
import com.fantasylol.backend.entity.SeasonSettlement;
import com.fantasylol.backend.entity.UserScore;
import com.fantasylol.backend.entity.WeeklySettlement;
import com.fantasylol.backend.repository.SeasonRepository;
import com.fantasylol.backend.repository.SeasonSettlementRepository;
import com.fantasylol.backend.repository.UserScoreRepository;
import com.fantasylol.backend.repository.WeeklySettlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementService {

    private final UserScoreRepository userScoreRepository;
    private final WeeklySettlementRepository weeklySettlementRepository;
    private final SeasonSettlementRepository seasonSettlementRepository;
    private final SeasonRepository seasonRepository;

    @Transactional
    public void settleWeek(String seasonName, int weekNumber) {

        if (weeklySettlementRepository.existsBySeasonNameAndWeekNumber(seasonName, weekNumber)) {
            log.info("Week already settled, skipping: {} week {}", seasonName, weekNumber);
            return;
        }

        Season season = seasonRepository.findBySeasonName(seasonName)
                .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 시즌입니다: " + seasonName));

        List<UserScore> scores = userScoreRepository
                .findByWeekNumberAndSeasonNameOrderByWeeklyScoreDesc(weekNumber, seasonName);

        int rank = 1;

        for (UserScore score : scores) {
            weeklySettlementRepository.save(WeeklySettlement.builder()
                    .user(score.getUser())
                    .season(season)
                    .weekNumber(weekNumber)
                    .totalScore(score.getWeeklyScore())
                    .rank(rank++)
                    .build());
        }

        log.info("Settled week {} ({}) — {} users ranked", weekNumber, seasonName, scores.size());

    }

    @Transactional
    public void settleSeason(String seasonName) {

        if (seasonSettlementRepository.existsBySeasonName(seasonName)) {
            log.info("Season already settled, skipping: {}", seasonName);
            return;
        }

        Season season = seasonRepository.findBySeasonName(seasonName)
                .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 시즌입니다: " + seasonName));

        List<UserScore> latestPerUser = userScoreRepository.findLatestPerUserBySeasonName(seasonName);

        int rank = 1;

        for (UserScore score : latestPerUser) {
            seasonSettlementRepository.save(SeasonSettlement.builder()
                    .user(score.getUser())
                    .season(season)
                    .totalScore(score.getSeasonalScore())
                    .rank(rank++)
                    .build());
        }

        log.info("Settled season {} — {} users ranked", seasonName, latestPerUser.size());

    }

}
