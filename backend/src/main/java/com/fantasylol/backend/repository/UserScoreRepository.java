package com.fantasylol.backend.repository;

import com.fantasylol.backend.entity.UserScore;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserScoreRepository extends JpaRepository<UserScore, Long> {

    @Query("SELECT us FROM UserScore us WHERE us.user.userId = :userId AND us.weekNumber = :weekNumber AND us.season.seasonName = :seasonName")
    Optional<UserScore> findByUserUserIdAndWeekNumberAndSeasonName(@Param("userId") Long userId, @Param("weekNumber") Integer weekNumber, @Param("seasonName") String seasonName);

    @Query("SELECT COALESCE(SUM(s.weeklyScore), 0) FROM UserScore s WHERE s.user.userId = :userId AND s.season.seasonName = :seasonName AND s.weekNumber <> :weekNumber")
    Double findSeasonalScoreExcludingWeekByUserIdAndSeasonName(@Param("userId") Long userId, @Param("seasonName") String seasonName, @Param("weekNumber") Integer weekNumber);

    @Query("SELECT us FROM UserScore us WHERE us.weekNumber = :weekNumber AND us.season.seasonName = :seasonName ORDER BY us.weeklyScore DESC")
    List<UserScore> findByWeekNumberAndSeasonNameOrderByWeeklyScoreDesc(@Param("weekNumber") Integer weekNumber, @Param("seasonName") String seasonName);

    @Query("SELECT COUNT(us) FROM UserScore us WHERE us.weekNumber = :weekNumber AND us.season.seasonName = :seasonName AND us.weeklyScore > :weeklyScore")
    long countByWeekNumberAndSeasonNameAndWeeklyScoreGreaterThan(@Param("weekNumber") Integer weekNumber, @Param("seasonName") String seasonName, @Param("weeklyScore") Double weeklyScore);

    Optional<UserScore> findTopByUserUserIdOrderByUpdatedAtDesc(Long userId);

    List<UserScore> findByUserUserId(Long userId);

    @Query("SELECT us FROM UserScore us WHERE us.weekNumber = :weekNumber AND us.season.seasonName = :seasonName ORDER BY us.weeklyScore DESC")
    Page<UserScore> findByWeekNumberAndSeasonNameOrderByWeeklyScoreDesc(@Param("weekNumber") Integer weekNumber, @Param("seasonName") String seasonName, Pageable pageable);

    @Query("""
        SELECT us FROM UserScore us
        WHERE us.season.seasonName = :seasonName
        AND us.weekNumber = (
            SELECT MAX(us2.weekNumber) FROM UserScore us2
            WHERE us2.user = us.user AND us2.season.seasonName = :seasonName
        )
        ORDER BY us.seasonalScore DESC
        """)
    Page<UserScore> findLatestPerUserBySeasonNameOrderBySeasonalScoreDesc(@Param("seasonName") String seasonName, Pageable pageable);

    @Query("""
        SELECT us FROM UserScore us
        WHERE us.user.userId = :userId
        AND us.season.seasonName = :seasonName
        AND us.weekNumber = (
            SELECT MAX(us2.weekNumber) FROM UserScore us2
            WHERE us2.user.userId = :userId AND us2.season.seasonName = :seasonName
        )
        """)
    Optional<UserScore> findTopByUserUserIdAndSeasonNameOrderByWeekNumberDesc(@Param("userId") Long userId, @Param("seasonName") String seasonName);

    @Query("""
        SELECT us FROM UserScore us
        WHERE us.season.seasonName = :seasonName
        AND us.weekNumber = (
            SELECT MAX(us2.weekNumber) FROM UserScore us2
            WHERE us2.user = us.user AND us2.season.seasonName = :seasonName
        )
        ORDER BY us.seasonalScore DESC
        """)
    List<UserScore> findLatestPerUserBySeasonName(@Param("seasonName") String seasonName);

    @Query("""
        SELECT COUNT(us) FROM UserScore us
        WHERE us.season.seasonName = :seasonName
        AND us.weekNumber = (
            SELECT MAX(us2.weekNumber) FROM UserScore us2
            WHERE us2.user = us.user AND us2.season.seasonName = :seasonName
        )
        AND us.seasonalScore > :score
        """)
    long countLatestPerUserBySeasonNameAndSeasonalScoreGreaterThan(@Param("seasonName") String seasonName, @Param("score") Double score);

}
