package com.fantasylol.backend.repository;

import com.fantasylol.backend.entity.WeeklySettlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WeeklySettlementRepository extends JpaRepository<WeeklySettlement, Long> {

    boolean existsBySeasonNameAndWeekNumber(String seasonName, Integer weekNumber);

    List<WeeklySettlement> findBySeasonNameAndWeekNumberOrderByRankAsc(String seasonName, Integer weekNumber);

}
