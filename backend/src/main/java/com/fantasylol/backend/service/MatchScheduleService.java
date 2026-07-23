package com.fantasylol.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchScheduleService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final LeaguepediaClient leaguepediaClient;

    public List<Map<String, String>> fetchUpcomingMatches() throws Exception {
        return fetchUpcomingMatches(null);
    }

    public List<Map<String, String>> fetchUpcomingMatches(String overviewPage) throws Exception {

        leaguepediaClient.login();

        String now = LocalDateTime.now().format(FORMATTER);
        String whereClause = "DateTime_UTC >= '" + now + "' AND (Winner IS NULL OR Winner = '')";

        if (overviewPage != null) {
            whereClause += " AND OverviewPage = '" + overviewPage + "'";
        }

        int limit = overviewPage == null ? 500 : 50;

        JsonNode matchSchedules = leaguepediaClient.cargoQuery(
                "MatchSchedule",
                "Team1,Team2,DateTime_UTC,BestOf,OverviewPage",
                whereClause,
                "DateTime_UTC",
                limit
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

        if (overviewPage == null || allUpcoming.isEmpty()) {
            return allUpcoming;
        }

        String nextMatchDate = allUpcoming.get(0).get("dateTimeUtc").substring(0, 10);

        List<Map<String, String>> upcoming = allUpcoming.stream()
                .takeWhile(m -> m.get("dateTimeUtc").startsWith(nextMatchDate))
                .toList();

        log.info("Found {} upcoming matches on {}", upcoming.size(), nextMatchDate);

        return upcoming;

    }

    public boolean isLastMatchOfWeek(String seasonName, LocalDate weekStart, LocalDate weekEnd) throws Exception {

        leaguepediaClient.login();

        String from = weekStart + " 00:00:00";
        String to = weekEnd + " 23:59:59";

        JsonNode result = leaguepediaClient.cargoQuery(
                "MatchSchedule",
                "Winner",
                "OverviewPage = '" + seasonName + "' AND DateTime_UTC >= '" + from + "' AND DateTime_UTC <= '" + to + "'",
                50
        );

        for (JsonNode node : result.path("cargoquery")) {

            String winner = node.path("title").path("Winner").asText();

            if (winner == null || winner.isBlank()) {
                return false;
            }

        }

        return true;

    }

}
