package com.fantasylol.backend.repository;

import com.fantasylol.backend.entity.WeeklyPlayerScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WeeklyPlayerScoreRepository extends JpaRepository<WeeklyPlayerScore, Long> {

    Optional<WeeklyPlayerScore> findByTeamTeamIdAndPlayerPlayerIdAndSeasonSeasonIdAndWeekNumber(
            Long teamId, Long playerId, Long seasonId, Integer weekNumber);

    List<WeeklyPlayerScore> findByTeamTeamIdAndSeasonSeasonIdAndWeekNumber(Long teamId, Long seasonId, Integer weekNumber);

    List<WeeklyPlayerScore> findByTeamTeamIdAndSeasonSeasonId(Long teamId, Long seasonId);

}
