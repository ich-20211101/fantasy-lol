package com.fantasylol.backend.repository;

import com.fantasylol.backend.entity.WeeklyStarter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface WeeklyStarterRepository extends JpaRepository<WeeklyStarter, Long> {

    // 점수 계산용 — 기존 findByPlayerPlayerIdInAndIsStarterTrue랑 같은 쓰임새,
    // 대상만 "지금 스타터"에서 "그 주차 스타터"로 바뀐 것
    List<WeeklyStarter> findByPlayerPlayerIdInAndWeekNumberAndSeasonName(Set<Long> playerIds, Integer weekNumber, String seasonName);

    // 스냅샷 저장 시 중복 방지용 체크 (DB의 UNIQUE 제약이 최종 방어선, 이건 사전 체크)
    boolean existsByTeamTeamIdAndWeekNumberAndSeasonName(Long teamId, Integer weekNumber, String seasonName);

    // 나중에 "내 라인업 히스토리 보기" 같은 기능에 바로 쓸 수 있음
    List<WeeklyStarter> findByTeamTeamIdOrderByWeekNumberDesc(Long teamId);

    Optional<WeeklyStarter> findTopByOrderByLockedAtDesc();

    @Query("SELECT DISTINCT ws.seasonName as seasonName, ws.weekNumber as weekNumber " +
            "FROM WeeklyStarter ws ORDER BY ws.weekNumber DESC")
    List<SeasonWeekView> findDistinctSeasonWeeks();

}
