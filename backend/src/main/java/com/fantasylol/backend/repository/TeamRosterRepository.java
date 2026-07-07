package com.fantasylol.backend.repository;

import com.fantasylol.backend.entity.TeamRoster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TeamRosterRepository extends JpaRepository<TeamRoster, Long> {
    List<TeamRoster> findByTeamTeamId(Long teamId);
    Optional<TeamRoster> findByTeamTeamIdAndPlayerPlayerId(Long teamId, Long playerId);
    int countByTeamTeamId(Long teamId);
    List<TeamRoster> findByPlayerPlayerIdInAndIsStarterTrue(Set<Long> playerIds);
}
