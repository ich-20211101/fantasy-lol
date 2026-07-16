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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchSyncService {

    private final LeaguepediaClient leaguepediaClient;
    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;
    private final PlayerStatRepository playerStatRepository;
    private final ScoreCalculator scoreCalculator;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final UserScoreService userScoreService;

    @Transactional
    public void syncByDate(LocalDate date) throws Exception {

        leaguepediaClient.login();

        String from = date + " 00:00:00";
        String to = date + " 23:59:59";

        // 1. 그날 매치 목록 조회
        JsonNode matchSchedules = leaguepediaClient.cargoQuery(
                "MatchSchedule",
                "Team1,Team2,Winner,OverviewPage,DateTime_UTC",
                "DateTime_UTC >= '" + from + "' AND DateTime_UTC <= '" + to + "' AND OverviewPage LIKE 'LCK/%'",
                50
        );

        JsonNode matchList = matchSchedules.path("cargoquery");

        if (matchList.isEmpty()) {
            log.info("No matches found: {}", date);
            return;
        }

        log.info("### Raw MatchSchedule: {}", matchList);

        // 2. 그날 전체 게임을 한 번에 조회 (매치별 반복 호출 제거)
        JsonNode gamesResponse = leaguepediaClient.cargoQuery(
                "ScoreboardGames",
                "GameId,Team1,Team2,DateTime_UTC,OverviewPage",
                "DateTime_UTC >= '" + from + "' AND DateTime_UTC <= '" + to + "' AND OverviewPage LIKE 'LCK/%'",
                50
        );

        Map<String, List<JsonNode>> gamesByMatchup = new HashMap<>();
        List<String> allGameIds = new ArrayList<>();

        for (JsonNode gameNode : gamesResponse.path("cargoquery")) {

            JsonNode g = gameNode.path("title");
            String key = matchupKey(g.path("OverviewPage").asText(), g.path("Team1").asText(), g.path("Team2").asText());

            gamesByMatchup.computeIfAbsent(key, k -> new ArrayList<>()).add(gameNode);
            allGameIds.add(g.path("GameId").asText());

        }

        // 3. 그날 전체 게임의 스탯을 GameId IN (...)으로 한 번에 조회
        Map<String, List<JsonNode>> statsByGameId = new HashMap<>();

        if (!allGameIds.isEmpty()) {

            String gameIdList = allGameIds.stream()
                    .map(id -> "'" + id + "'")
                    .collect(Collectors.joining(","));

            JsonNode statsResponse = leaguepediaClient.cargoQuery(
                    "ScoreboardPlayers",
                    "GameId,Name,Team,Role,Champion,Kills,Deaths,Assists,Gold,CS,DamageToChampions,VisionScore,PlayerWin",
                    "GameId IN (" + gameIdList + ")",
                    allGameIds.size() * 10
            );

            for (JsonNode statNode : statsResponse.path("cargoquery")) {
                String gameId = statNode.path("title").path("GameId").asText();
                statsByGameId.computeIfAbsent(gameId, k -> new ArrayList<>()).add(statNode);
            }

        }

        // 4. 매치별로 로컬 메모리에서 엮어서 저장
        for (JsonNode matchNode : matchList) {

            try {
                syncMatch(matchNode, gamesByMatchup, statsByGameId);
            } catch (Exception e) {
                log.error("Failed to sync match: {}", matchNode, e);
            }

        }

        log.info("Sync completed for date: {}", date);

    }

    private String matchupKey(String overviewPage, String team1, String team2) {
        return overviewPage + "|" + team1 + "|" + team2;
    }

    private void syncMatch(JsonNode matchNode, Map<String, List<JsonNode>> gamesByMatchup, Map<String, List<JsonNode>> statsByGameId) throws Exception {

        JsonNode t = matchNode.path("title");

        String team1 = t.path("Team1").asText();
        String team2 = t.path("Team2").asText();
        String winner = t.path("Winner").asText();
        String winnerTeam = winner.equals("1") ? team1 : team2;
        String overviewPage = t.path("OverviewPage").asText();
        String dateTimeStr = t.path("DateTime UTC").asText();

        List<JsonNode> gameList = gamesByMatchup.get(matchupKey(overviewPage, team1, team2));

        if (gameList == null || gameList.isEmpty()) {
            log.info("No games found for match: {} vs {}", team1, team2);
            return;
        }

        String firstGameId = gameList.get(0).path("title").path("GameId").asText();
        String matchId = firstGameId.substring(0, firstGameId.lastIndexOf("_"));

        Match match = matchRepository.findByLeaguepediaMatchId(matchId)
                .orElseGet(() -> matchRepository.save(Match.builder()
                        .leaguepediaMatchId(matchId)
                        .seasonName(overviewPage)
                        .team1(team1)
                        .team2(team2)
                        .matchDate(LocalDateTime.parse(dateTimeStr, FORMATTER))
                        .status("COMPLETED")
                        .winner(winnerTeam)
                        .build()));

        for (JsonNode game : gameList) {

            String gameId = game.path("title").path("GameId").asText();
            syncGameStats(match, gameId, statsByGameId.getOrDefault(gameId, List.of()));

        }

        log.info("Synced match: {} vs {} | Winner: {}", team1, team2, winnerTeam);

        userScoreService.updateScoresForMatch(match);

    }

    private void syncGameStats(Match match, String gameId, List<JsonNode> statNodes) throws Exception {

        int gameNumber = extractGameNumber(gameId);

        for (JsonNode stat : statNodes) {

            JsonNode s = stat.path("title");
            String rawName = s.path("Name").asText();
            String playerName = sanitizePlayerName(rawName);
            String teamName = s.path("Team").asText();
            String leaguepediaGameId = gameId + "-" + rawName;

            if (playerStatRepository.findByLeaguepediaGameId(leaguepediaGameId).isPresent()) {
                continue;
            }

            Player player = playerRepository.findByPlayerNameAndTeamName(playerName, teamName)
                    .orElseGet(() -> playerRepository.save(Player.builder()
                            .playerName(playerName)
                            .teamName(teamName)
                            .position(s.path("Role").asText())
                            .build()));

            PlayerStat playerStat = PlayerStat.builder()
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
                    .build();

            playerStat.setActualScore(scoreCalculator.calculateActualScore(playerStat));
            playerStatRepository.save(playerStat);

        }

        log.info("Synced game {}", gameId);

    }

    private String sanitizePlayerName(String name) {

        if (name.contains("(")) {
            return name.substring(0, name.indexOf("(")).trim();
        }

        return name;

    }

    private int extractGameNumber(String gameId) {

        try {
            return Integer.parseInt(gameId.substring(gameId.lastIndexOf("_") + 1));
        } catch (Exception e) {
            return 1;
        }

    }

    public List<Map<String, String>> fetchUpcomingMatches() throws Exception {

        leaguepediaClient.login();

        String now = LocalDateTime.now().format(FORMATTER);

        JsonNode matchSchedules = leaguepediaClient.cargoQuery(
                "MatchSchedule",
                "Team1,Team2,DateTime_UTC,BestOf,OverviewPage",
                "DateTime_UTC >= '" + now + "' AND OverviewPage LIKE 'LCK/%' AND (Winner IS NULL OR Winner = '')",
                "DateTime_UTC",
                50
        );

        List<Map<String, String>> allUpcoming = new ArrayList<>();

        for (JsonNode node : matchSchedules.path("cargoquery")) {

            JsonNode t = node.path("title");

            allUpcoming.add(Map.of(
                    "team1", t.path("Team1").asText(),
                    "team2", t.path("Team2").asText(),
                    "dateTimeUtc", t.path("DateTime UTC").asText(),
                    "bestOf", t.path("BestOf").asText(),
                    "overviewPage", t.path("OverviewPage").asText()
            ));

        }

        if (allUpcoming.isEmpty()) {
            return allUpcoming;
        }

        String nextMatchDate = allUpcoming.get(0).get("dateTimeUtc").substring(0, 10);

        List<Map<String, String>> upcoming = allUpcoming.stream()
                .takeWhile(m -> m.get("dateTimeUtc").startsWith(nextMatchDate))
                .toList();

        log.info("Found {} upcoming matches on {}", upcoming.size(), nextMatchDate);

        return upcoming;

    }

}
