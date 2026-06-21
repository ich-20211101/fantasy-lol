package com.fantasylol.backend.service;

import com.fantasylol.backend.entity.Player;
import com.fantasylol.backend.repository.PlayerRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerSyncService {

    private final LeaguepediaClient leaguepediaClient;
    private final PlayerRepository playerRepository;

    @Transactional
    public void syncPlayers(String overviewPage) throws Exception {

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
            return;
        }

        int count = 0;

        for (JsonNode node : playerList) {

            JsonNode p = node.path("title");
            String playerName = p.path("Player").asText();
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

    }

}
