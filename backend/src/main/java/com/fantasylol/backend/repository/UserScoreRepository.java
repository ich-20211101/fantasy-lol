package com.fantasylol.backend.repository;

import com.fantasylol.backend.entity.UserScore;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserScoreRepository extends JpaRepository<UserScore, Long> {
    Optional<UserScore> findByUserUserIdAndWeekNumberAndSeasonName(Long userId, Integer weekNumber, String seasonName);

    @Query("SELECT COALESCE(SUM(s.weeklyScore), 0) FROM UserScore s WHERE s.user.userId = :userId AND s.seasonName = :seasonName")
    Double findSeasonalScoreByUserIdAndSeasonName(@Param("userId") Long userId, @Param("seasonName") String seasonName);

    List<UserScore> findByWeekNumberAndSeasonNameOrderByWeeklyScoreDesc(Integer weekNumber, String seasonName);

    Optional<UserScore> findTopByUserUserIdOrderByUpdatedAtDesc(Long userId);

    List<UserScore> findByUserUserId(Long userId);
}
