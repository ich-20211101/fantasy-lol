package com.fantasylol.backend.repository;

import com.fantasylol.backend.entity.WeeklyStarter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface WeeklyStarterRepository extends JpaRepository<WeeklyStarter, Long> {

    // 점수 계산용 — 기존 findByPlayerPlayerIdInAndIsStarterTrue랑 같은 쓰임새,
    // 대상만 "지금 스타터"에서 "그 주차 스타터"로 바뀐 것
    List<WeeklyStarter> findByPlayerPlayerIdInAndWeekNumberAndSeasonSeasonId(Set<Long> playerIds, Integer weekNumber, Long seasonId);

    // 스냅샷 저장 시 중복 방지용 체크 (DB의 UNIQUE 제약이 최종 방어선, 이건 사전 체크)
    // 스타터 변경 가능 여부(락 체크)에도 그대로 재사용됨 — WeeklyStarter 존재 = 그 주차는 잠긴 것
    boolean existsByTeamTeamIdAndWeekNumberAndSeasonSeasonId(Long teamId, Integer weekNumber, Long seasonId);

    // 나중에 "내 라인업 히스토리 보기" 같은 기능에 바로 쓸 수 있음
    List<WeeklyStarter> findByTeamTeamIdOrderByWeekNumberDesc(Long teamId);

    Optional<WeeklyStarter> findTopByOrderByLockedAtDesc();

    @Query("SELECT DISTINCT ws.team.user.userId FROM WeeklyStarter ws WHERE ws.weekNumber = :weekNumber AND ws.season.seasonName = :seasonName")
    List<Long> findDistinctUserIdsByWeekNumberAndSeasonName(@Param("weekNumber") Integer weekNumber, @Param("seasonName") String seasonName);

    @Query("SELECT DISTINCT ws.team.user.userId FROM WeeklyStarter ws WHERE ws.season.seasonName = :seasonName")
    List<Long> findDistinctUserIdsBySeasonName(@Param("seasonName") String seasonName);

    @Query("SELECT DISTINCT ws.season.seasonName as seasonName, ws.weekNumber as weekNumber FROM WeeklyStarter ws ORDER BY ws.weekNumber DESC")
    List<SeasonWeekView> findDistinctSeasonWeeks();

}
