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

    Optional<UserScore> findByUserUserIdAndWeekNumberAndSeasonName(Long userId, Integer weekNumber, String seasonName);

    @Query("SELECT COALESCE(SUM(s.weeklyScore), 0) FROM UserScore s WHERE s.user.userId = :userId AND s.seasonName = :seasonName")
    Double findSeasonalScoreByUserIdAndSeasonName(@Param("userId") Long userId, @Param("seasonName") String seasonName);

    List<UserScore> findByWeekNumberAndSeasonNameOrderByWeeklyScoreDesc(Integer weekNumber, String seasonName);

    long countByWeekNumberAndSeasonNameAndWeeklyScoreGreaterThan(Integer weekNumber, String seasonName, Double weeklyScore);

    Optional<UserScore> findTopByUserUserIdOrderByUpdatedAtDesc(Long userId);

    List<UserScore> findByUserUserId(Long userId);

    Page<UserScore> findByWeekNumberAndSeasonNameOrderByWeeklyScoreDesc(Integer weekNumber, String seasonName, Pageable pageable);

    @Query("""
        SELECT us FROM UserScore us
        WHERE us.seasonName = :seasonName
        AND us.weekNumber = (
            SELECT MAX(us2.weekNumber) FROM UserScore us2
            WHERE us2.user = us.user AND us2.seasonName = :seasonName
        )
        ORDER BY us.seasonalScore DESC
        """)
    Page<UserScore> findLatestPerUserBySeasonNameOrderBySeasonalScoreDesc(@Param("seasonName") String seasonName, Pageable pageable);

    Optional<UserScore>
    findTopByUserUserIdAndSeasonNameOrderByWeekNumberDesc(Long userId, String seasonName);
    @Query("""
        SELECT COUNT(us) FROM UserScore us
        WHERE us.seasonName = :seasonName
        AND us.weekNumber = (
            SELECT MAX(us2.weekNumber) FROM UserScore us2
            WHERE us2.user = us.user AND us2.seasonName = :seasonName
        )
        AND us.seasonalScore > :score
        """)
    long countLatestPerUserBySeasonNameAndSeasonalScoreGreaterThan(@Param("seasonName") String seasonName, @Param("score") Double score);


}
