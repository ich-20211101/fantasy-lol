package com.fantasylol.backend.repository;

import com.fantasylol.backend.entity.SeasonWeek;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SeasonWeekRepository extends JpaRepository<SeasonWeek, Long> {
    Optional<SeasonWeek> findBySeasonSeasonIdAndWeekNumber(Long seasonId, Integer weekNumber);
}
