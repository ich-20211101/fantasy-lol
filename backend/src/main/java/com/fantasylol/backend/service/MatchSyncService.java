package com.fantasylol.backend.service;

import com.fantasylol.backend.entity.Match;
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

        JsonNode games = leaguepediaClient.cargoQuery(
                "ScoreboardGames",
                "GameId,Team1,Team2,DateTime_UTC,OverviewPage,Winner,BestOf",
                "DateTime_UTC >= '" + from + "' AND DateTime_UTC <= '" + to + "'",
                50
        );

        JsonNode gameList = games.path("cargoquery");

        if (gameList.isEmpty()) {
            log.info("No games found: {}", date);
            return;
        }

        for (JsonNode game : gameList) {
            JsonNode title = game.path("title");
            String gameId = title.path("GameId").asText();
            String team1 = title.path("Team1").asText();
            String team2 = title.path("Team2").asText();
            String dateTimeStr = title.path("DateTime UTC").asText();
            String overviewPage = title.path("OverviewPage").asText();
            String winner = title.path("Winner").asText();
            int bestOf = title.path("BestOf").asInt(1);
            int gameNumber = extractGameNumber(gameId);

            String matchId = gameId.substring(0, gameId.lastIndexOf("_"));

            Match match = matchRepository.findByLeaguepediaMatchId(matchId)
                    .orElseGet(() -> matchRepository.save(Match.builder()
                            .leaguepediaMatchId(matchId)
                            .seasonName(overviewPage)
                            .team1(team1)
                            .team2(team2)
                            .matchDate(LocalDateTime.parse(dateTimeStr, formatter))
                            .bestOf(bestOf)
                            .status("COMPLETED")
                            .winner(winner.equals("1") ? team1 : team2)
                            .build()));

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

            }

        }

    }

    private int extractGameNumber(String gameId) {

        try {
            return Integer.parseInt(gameId.substring(gameId.lastIndexOf("_") + 1));
        } catch (Exception e) {
            return 1;
        }

    }

}
