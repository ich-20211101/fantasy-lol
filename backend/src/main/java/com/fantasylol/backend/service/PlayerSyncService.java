package com.fantasylol.backend.service;

import com.fantasylol.backend.entity.Player;
import com.fantasylol.backend.repository.PlayerRepository;
import com.fantasylol.backend.util.PlayerNameSanitizer;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerSyncService {

    private final LeaguepediaClient leaguepediaClient;
    private final PlayerRepository playerRepository;

    private static final Pattern OVERVIEW_PAGE_PATTERN = Pattern.compile("^LCK( CL)?/\\d{4} Season/.+$");

    @CacheEvict(cacheNames = "players", allEntries = true)
    @Transactional
    public int syncPlayers(String overviewPage) throws Exception {

        if (!OVERVIEW_PAGE_PATTERN.matcher(overviewPage).matches()) {
            throw new IllegalArgumentException("overviewPage 형식이 올바르지 않습니다. 예) LCK/2026 Season/Rounds 1-2");
        }

        leaguepediaClient.login();

        JsonNode response = leaguepediaClient.cargoQuery(
                "TournamentPlayers",
                "Player,Team,Role,OverviewPage",
                "OverviewPage='" + overviewPage + "' AND Role IN ('Top','Jungle','Mid','Bot','Support')",
                100
        );

        JsonNode playerList = response.path("cargoquery");

        if (playerList.isEmpty()) {
            log.info("No players found for: {}", overviewPage);
            return 0;
        }

        int count = 0;

        for (JsonNode node : playerList) {

            JsonNode p = node.path("title");
            String playerName = PlayerNameSanitizer.sanitize(p.path("Player").asText());
            String teamName = p.path("Team").asText();
            String role = p.path("Role").asText();

            playerRepository.findByPlayerNameAndTeamName(playerName, teamName)
                    .orElseGet(() -> playerRepository.save(Player.builder()
                            .playerName(playerName)
                            .teamName(teamName)
                            .position(role)
                            .build()));

            count ++;

        }

        log.info("Synced {} players for: {}", count, overviewPage);

        return count;

    }

}
