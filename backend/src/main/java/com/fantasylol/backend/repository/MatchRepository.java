package com.fantasylol.backend.repository;

import com.fantasylol.backend.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long> {
    Optional<Match> findByLeaguepediaMatchId(String leaguepediaMatchId);
}
