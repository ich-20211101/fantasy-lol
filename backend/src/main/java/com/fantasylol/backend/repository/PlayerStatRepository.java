package com.fantasylol.backend.repository;

import com.fantasylol.backend.entity.PlayerStat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlayerStatRepository extends JpaRepository<PlayerStat, Long> {
    Optional<PlayerStat> findByLeaguepediaGameId(String leaguepediaMatchId);
    List<PlayerStat> findByMatchMatchId(Long matchId);
}
