package com.fantasylol.backend.repository;

import com.fantasylol.backend.entity.WeeklySettlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WeeklySettlementRepository extends JpaRepository<WeeklySettlement, Long> {

    @Query("SELECT CASE WHEN COUNT(ws) > 0 THEN true ELSE false END FROM WeeklySettlement ws WHERE ws.season.seasonName = :seasonName AND ws.weekNumber = :weekNumber")
    boolean existsBySeasonNameAndWeekNumber(@Param("seasonName") String seasonName, @Param("weekNumber") Integer weekNumber);

    @Query("SELECT ws FROM WeeklySettlement ws WHERE ws.season.seasonName = :seasonName AND ws.weekNumber = :weekNumber ORDER BY ws.rank ASC")
    List<WeeklySettlement> findBySeasonNameAndWeekNumberOrderByRankAsc(@Param("seasonName") String seasonName, @Param("weekNumber") Integer weekNumber);

}
