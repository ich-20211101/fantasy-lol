package com.fantasylol.backend.repository;

import com.fantasylol.backend.entity.Player;
import com.fantasylol.backend.entity.PlayerStat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlayerStatRepository extends JpaRepository<PlayerStat, Long> {

    Optional<PlayerStat> findByLeaguepediaGameId(String leaguepediaMatchId);

    List<PlayerStat> findByMatchMatchId(Long matchId);

    interface PlayerRankingRow {
        Player getPlayer();
        Double getTotalScore();
    }

    @Query("""
        SELECT ps.player AS player, SUM(ps.actualScore) AS totalScore
        FROM PlayerStat ps
        WHERE ps.match.seasonName = :seasonName
        AND (:position = 'ALL' OR ps.player.position = :position)
        GROUP BY ps.player
        ORDER BY SUM(ps.actualScore) DESC
        """)
    Page<PlayerRankingRow> findPlayerRankings(@Param("seasonName") String seasonName, @Param("position") String position, Pageable pageable);

}
