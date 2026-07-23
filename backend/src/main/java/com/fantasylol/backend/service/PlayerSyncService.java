package com.fantasylol.backend.service;

import com.fantasylol.backend.entity.ProTeam;
import com.fantasylol.backend.entity.Player;
import com.fantasylol.backend.repository.PlayerRepository;
import com.fantasylol.backend.repository.ProTeamRepository;
import com.fantasylol.backend.repository.SeasonRepository;
import com.fantasylol.backend.util.PlayerNameSanitizer;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerSyncService {

    private final LeaguepediaClient leaguepediaClient;
    private final PlayerRepository playerRepository;
    private final SeasonRepository seasonRepository;
    private final ProTeamRepository proTeamRepository;

    @CacheEvict(cacheNames = "players", allEntries = true)
    @Transactional
    public int syncPlayers(String overviewPage) throws Exception {

        if (!seasonRepository.existsBySeasonName(overviewPage)) {
            throw new IllegalArgumentException("등록되지 않은 시즌입니다. 먼저 시즌을 등록해주세요: " + overviewPage);
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

        Set<String> knownTeams = proTeamRepository.findAllByOrderByFullNameAsc().stream()
                .map(ProTeam::getFullName)
                .collect(Collectors.toSet());

        boolean filterByKnownTeam = !knownTeams.isEmpty();

        int count = 0;

        for (JsonNode node : playerList) {

            JsonNode p = node.path("title");
            String playerName = PlayerNameSanitizer.sanitize(p.path("Player").asText());
            String teamName = p.path("Team").asText();
            String role = p.path("Role").asText();

            if (filterByKnownTeam && !knownTeams.contains(teamName)) {
                continue;
            }

            Player player = playerRepository.findByPlayerName(playerName)
                    .map(existing -> {
                        existing.setTeamName(teamName);
                        existing.setPosition(role);
                        existing.setCurrentSeasonName(overviewPage);
                        return existing;
                    })
                    .orElseGet(() -> Player.builder()
                            .playerName(playerName)
                            .teamName(teamName)
                            .position(role)
                            .currentSeasonName(overviewPage)
                            .build());

            playerRepository.save(player);

            count ++;

        }

        log.info("Synced {} players for: {}", count, overviewPage);

        return count;

    }

}
