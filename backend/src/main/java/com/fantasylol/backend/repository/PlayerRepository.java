package com.fantasylol.backend.repository;

import com.fantasylol.backend.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    Optional<Player> findByPlayerNameAndTeamName(String playerName, String teamName);

    @Query("SELECT DISTINCT p.teamName FROM Player p")
    List<String> findDistinctTeamNames();

}
