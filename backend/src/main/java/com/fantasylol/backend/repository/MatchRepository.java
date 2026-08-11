package com.fantasylol.backend.repository;

import com.fantasylol.backend.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long> {
    Optional<Match> findByLeaguepediaMatchId(String leaguepediaMatchId);
    Optional<Match> findFirstByStatusOrderByMatchDateDesc(String status);
    List<Match> findByStatusAndMatchDateBetweenOrderByMatchDateAsc(String status, LocalDateTime from, LocalDateTime to);
}
