package com.fantasylol.backend.repository;

import com.fantasylol.backend.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByPlayerNameAndTeamName(String playerName, String teamName);
}
