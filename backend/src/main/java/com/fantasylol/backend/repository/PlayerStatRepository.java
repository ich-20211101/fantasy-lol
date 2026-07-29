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
        Double getAvgScore();
        Long getGamesPlayed();
        Boolean getQualified();
    }

    @Query("""
        SELECT ps.player AS player,
            AVG(ps.actualScore) AS avgScore,
            COUNT(ps) AS gamesPlayed,
            CASE WHEN COUNT(ps) >= :minGames THEN true ELSE false END AS qualified
        FROM PlayerStat ps
        WHERE ps.match.seasonName = :seasonName
        AND (:position = 'ALL' OR ps.player.position = :position)
        GROUP BY ps.player
        ORDER BY CASE WHEN COUNT(ps) >= :minGames THEN 0 ELSE 1 END, AVG(ps.actualScore) DESC
        """)
    Page<PlayerRankingRow> findPlayerRankings(@Param("seasonName") String seasonName,
                                              @Param("position") String position,
                                              @Param("minGames") long minGames,
                                              Pageable pageable);

    interface PlayerSeasonAggregate {
        Player getPlayer();
        Double getAvgScore();
        Long getGamesPlayed();
    }

    @Query("""
        SELECT ps.player AS player, AVG(ps.actualScore) AS avgScore, COUNT(ps) AS gamesPlayed
        FROM PlayerStat ps
        WHERE ps.match.seasonName = :seasonName
        GROUP BY ps.player
    """)
    List<PlayerSeasonAggregate> findSeasonAggregates(@Param("seasonName") String seasonName);

}
