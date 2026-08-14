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
        Long getMatchesPlayed();
    }

    @Query("""
        SELECT ps.player AS player,
            AVG(ps.actualScore) AS avgScore,
            COUNT(DISTINCT ps.match) AS matchesPlayed
        FROM PlayerStat ps
        WHERE ps.match.season.seasonName = :seasonName
        AND (:position = 'ALL' OR ps.player.position = :position)
        GROUP BY ps.player
        ORDER BY AVG(ps.actualScore) DESC
        """
    )
    Page<PlayerRankingRow> findPlayerRankings(@Param("seasonName") String seasonName,
                                              @Param("position") String position,
                                              Pageable pageable);

    interface PlayerSeasonAggregate {
        Player getPlayer();
        Double getAvgScore();
        Double getTotalScore();
        Long getMatchesPlayed();
    }

    @Query("""
            SELECT ps.player AS player, AVG(ps.actualScore) AS avgScore, SUM(ps.actualScore) AS totalScore, COUNT(DISTINCT ps.match) AS matchesPlayed
            FROM PlayerStat ps
            WHERE ps.match.season.seasonName = :seasonName
            GROUP BY ps.player
        """)
    List<PlayerSeasonAggregate> findSeasonAggregates(@Param("seasonName") String seasonName);

    interface MatchTeamGameWins {
        Long getMatchId();
        String getTeam();
        Long getGamesWon();
    }

    @Query("""
        SELECT ps.match.matchId AS matchId, ps.team AS team, COUNT(DISTINCT ps.gameNumber) AS gamesWon
        FROM PlayerStat ps
        WHERE ps.match.matchId IN :matchIds AND ps.playerWin = true
        GROUP BY ps.match.matchId, ps.team
        """)
    List<MatchTeamGameWins> findGameWinsByMatchIds(@Param("matchIds") List<Long> matchIds);

}
