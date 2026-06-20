package com.fantasylol.backend.service;

import com.fantasylol.backend.entity.Match;
import com.fantasylol.backend.entity.Player;
import com.fantasylol.backend.entity.PlayerStat;
import com.fantasylol.backend.repository.MatchRepository;
import com.fantasylol.backend.repository.PlayerRepository;
import com.fantasylol.backend.repository.PlayerStatRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchSyncService {

    private final LeaguepediaClient leaguepediaClient;
    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;
    private final PlayerStatRepository playerStatRepository;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Transactional
    public void syncByDate(LocalDate date) throws Exception {

        leaguepediaClient.login();

        String from = date + " 00:00:00";
        String to = date + " 23:59:59";

        // MatchSchedule에서 매치 목록 조회
        JsonNode matchSchedules = leaguepediaClient.cargoQuery(
                "MatchSchedule",
                "Team1,Team2,Winner,OverviewPage,DateTime_UTC",
                "DateTime_UTC >= '" + from + "' AND DateTime_UTC <= '" + to + "' AND OverviewPage LIKE '%LCK%'",
                50
        );

        JsonNode matchList = matchSchedules.path("cargoquery");

        if (matchSchedules.isEmpty()) {
            log.info("No matches found: {}", date);
            return;
        }

        for (JsonNode matchNode : matchList) {
            JsonNode t = matchNode.path("title");
            String team1 = t.path("Team1").asText();
            String team2 = t.path("Team2").asText();
            String winner = t.path("Winner").asText();
            String winnerTeam = winner.equals("1") ? team1 : team2;
            String overviewPage = t.path("OverviewPage").asText();
            String dateTimeStr = t.path("DateTime UTC").asText();

            // ScoreboardGames에서 해당 매치 게임 목록 조회
            JsonNode games = leaguepediaClient.cargoQuery(
                    "ScoreboardGames",
                    "GameId,Team1,Team2,DateTime_UTC",
                    "OverviewPage='" + overviewPage + "' AND Team1='" + team1 + "' AND Team2='" + team2 + "'",
                    10
            );

            JsonNode gameList = games.path("cargoquery");

            if (gameList.isEmpty()) {
                log.info("No games found for match: {} vs {}", team1, team2);
                continue;
            }

            String matchId = gameList.get(0).path("title").path("GameId").asText();
            matchId = matchId.substring(0, matchId.lastIndexOf("_"));

            // Match 저장
            final String finalMatchId = matchId;
            final String finalWinnerTeam = winnerTeam;
            Match match = matchRepository.findByLeaguepediaMatchId(matchId)
                    .orElseGet(() -> matchRepository.save(Match.builder()
                            .leaguepediaMatchId(finalMatchId)
                            .seasonName(overviewPage)
                            .team1(team1)
                            .team2(team2)
                            .matchDate(LocalDateTime.parse(dateTimeStr, formatter))
                            .status("COMPLETED")
                            .winner(finalWinnerTeam)
                            .build()));

            // 각 게임 PlayerStat 저장
            for (JsonNode game : gameList) {

                String gameId = game.path("title").path("GameId").asText();
                int gameNumber = extractGameNumber(gameId);

                JsonNode statsResponse = leaguepediaClient.cargoQuery(
                        "ScoreboardPlayers",
                        "Name,Team,Role,Champion,Kills,Deaths,Assists,Gold,CS,DamageToChampions,VisionScore,PlayerWin",
                        "GameId='" + gameId + "'",
                        10
                );

                for (JsonNode stat : statsResponse.path("cargoquery")) {
                    JsonNode s = stat.path("title");
                    String playerName = s.path("Name").asText();
                    String teamName = s.path("Team").asText();
                    String leaguepediaGameId = gameId + "-" + playerName;

                    if (playerStatRepository.findByLeaguepediaGameId(leaguepediaGameId).isPresent()) {
                        continue;
                    }

                    Player player = playerRepository.findByPlayerNameAndTeamName(playerName, teamName)
                            .orElseGet(() -> playerRepository.save(Player.builder()
                                    .playerName(playerName)
                                    .teamName(teamName)
                                    .position(s.path("Role").asText())
                                    .build()));

                    playerStatRepository.save(PlayerStat.builder()
                            .match(match)
                            .gameNumber(gameNumber)
                            .player(player)
                            .leaguepediaGameId(leaguepediaGameId)
                            .team(teamName)
                            .role(s.path("Role").asText())
                            .champion(s.path("Champion").asText())
                            .kills(s.path("Kills").asInt(0))
                            .deaths(s.path("Deaths").asInt(0))
                            .assists(s.path("Assists").asInt(0))
                            .gold(s.path("Gold").asInt(0))
                            .cs(s.path("CS").asInt(0))
                            .damageToChampions(s.path("DamageToChampions").asInt(0))
                            .visionScore(s.path("VisionScore").asInt(0))
                            .playerWin("Yes".equals(s.path("PlayerWin").asText()))
                            .build());

                }

                log.info("Synced game {}", gameId);

            }

            log.info("Synced match: {} vs {} | Winner: {}", team1, team2, winnerTeam);

        }

        log.info("Sync completed for date: {}", date);

    }

    private int extractGameNumber(String gameId) {

        try {
            return Integer.parseInt(gameId.substring(gameId.lastIndexOf("_") + 1));
        } catch (Exception e) {
            return 1;
        }

    }

}
