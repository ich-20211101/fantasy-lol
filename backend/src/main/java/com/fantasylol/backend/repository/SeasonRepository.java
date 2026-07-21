package com.fantasylol.backend.repository;

import com.fantasylol.backend.entity.Season;
import com.fantasylol.backend.entity.SeasonStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SeasonRepository extends JpaRepository<Season, Long> {
    Optional<Season> findBySeasonName(String seasonName);
    boolean existsBySeasonName(String seasonName);
    List<Season> findByStatusAndStartDateLessThanEqual(SeasonStatus status, LocalDate date);
    List<Season> findByStatus(SeasonStatus status);
    List<Season> findByStatusInOrderByStartDateDesc(List<SeasonStatus> statuses);
}
