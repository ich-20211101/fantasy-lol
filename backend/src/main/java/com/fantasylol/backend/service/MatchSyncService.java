package com.fantasylol.backend.service;

import com.fantasylol.backend.entity.Season;
import com.fantasylol.backend.entity.Match;
import com.fantasylol.backend.entity.Player;
import com.fantasylol.backend.entity.PlayerStat;
import com.fantasylol.backend.repository.MatchRepository;
import com.fantasylol.backend.repository.PlayerRepository;
import com.fantasylol.backend.repository.PlayerStatRepository;
import com.fantasylol.backend.repository.SeasonRepository;
import com.fantasylol.backend.util.KstTime;
import com.fantasylol.backend.util.PlayerNameSanitizer;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
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
    private final SeasonWeekService seasonWeekService;
    private final SeasonRepository seasonRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final UserScoreService userScoreService;

    @Transactional
    public void syncByDate(LocalDate date) throws Exception {

        leaguepediaClient.login();

        ZonedDateTime fromKst = date.atStartOfDay(KstTime.KST);
        ZonedDateTime toKst = date.plusDays(1).atStartOfDay(KstTime.KST).minusSeconds(1);

        String from = fromKst.withZoneSameInstant(ZoneOffset.UTC).format(FORMATTER);
        String to = toKst.withZoneSameInstant(ZoneOffset.UTC).format(FORMATTER);

        List<String> registeredSeasonNames = seasonRepository.findAll().stream()
                .map(Season::getSeasonName)
                .toList();

        if (registeredSeasonNames.isEmpty()) {
            log.info("등록된 시즌이 없어 동기화 스킵: {}", date);
            return;
        }

        String overviewPageFilter = registeredSeasonNames.stream()
                .map(name -> "'" + name + "'")
                .collect(Collectors.joining(","));

        String whereClause = "DateTime_UTC >= '" + from + "' AND DateTime_UTC <= '" + to + "'" + " AND OverviewPage IN (" + overviewPageFilter + ")";



        // 1. 그날 매치 목록 조회
        JsonNode matchSchedules = leaguepediaClient.cargoQuery(
                "MatchSchedule",
                "Team1,Team2,Winner,OverviewPage,DateTime_UTC",
                whereClause,
                50
        );

        JsonNode matchList = matchSchedules.path("cargoquery");

        log.info("### Raw MatchSchedule: {}", matchList);

        if (matchList.isEmpty()) {
            log.info("No matches found: {}", date);
            return;
        }

        // 2. 그날 전체 게임을 한 번에 조회 (매치별 반복 호출 제거)
        JsonNode gamesResponse = leaguepediaClient.cargoQuery(
                "ScoreboardGames",
                "GameId,Team1,Team2,DateTime_UTC,OverviewPage",
                whereClause,
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
        String[] teams = {team1, team2};
        Arrays.sort(teams);
        return overviewPage + "|" + teams[0] + "|" + teams[1];
    }

    private void syncMatch(JsonNode matchNode, Map<String, List<JsonNode>> gamesByMatchup, Map<String, List<JsonNode>> statsByGameId) throws Exception {

        JsonNode t = matchNode.path("title");

        String team1 = t.path("Team1").asText();
        String team2 = t.path("Team2").asText();
        String winner = t.path("Winner").asText();
        String winnerTeam = winner.equals("1") ? team1 : team2;
        String overviewPage = t.path("OverviewPage").asText();
        String dateTimeStr = t.path("DateTime UTC").asText();

        if (!seasonRepository.existsBySeasonName(overviewPage)) {
            return;
        }

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
        seasonWeekService.checkAndFinalizeWeek(match);

    }

    private void syncGameStats(Match match, String gameId, List<JsonNode> statNodes) throws Exception {

        int gameNumber = extractGameNumber(gameId);

        for (JsonNode stat : statNodes) {

            JsonNode s = stat.path("title");
            String rawName = s.path("Name").asText();
            String playerName = PlayerNameSanitizer.sanitize(rawName);
            String teamName = s.path("Team").asText();
            String leaguepediaGameId = gameId + "-" + rawName;

            if (playerStatRepository.findByLeaguepediaGameId(leaguepediaGameId).isPresent()) {
                continue;
            }

            Player player = playerRepository.findByPlayerName(playerName)
                    .map(existing -> {
                        existing.setTeamName(teamName);
                        existing.setPosition(s.path("Role").asText());
                        existing.setCurrentSeasonName(match.getSeasonName());
                        return existing;
                    })
                    .orElseGet(() -> Player.builder()
                            .playerName(playerName)
                            .teamName(teamName)
                            .position(s.path("Role").asText())
                            .currentSeasonName(match.getSeasonName())
                            .build());

            player = playerRepository.save(player);

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

    private int extractGameNumber(String gameId) {

        try {
            return Integer.parseInt(gameId.substring(gameId.lastIndexOf("_") + 1));
        } catch (Exception e) {
            return 1;
        }

    }

}
