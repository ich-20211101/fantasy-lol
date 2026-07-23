package com.fantasylol.backend.service;

import com.fantasylol.backend.dto.PlayerRankingDto;
import com.fantasylol.backend.entity.Player;
import com.fantasylol.backend.entity.Season;
import com.fantasylol.backend.repository.PlayerRepository;
import com.fantasylol.backend.repository.PlayerStatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerService {

    private static final int MAX_PAGE_SIZE = 50;

    private final PlayerRepository playerRepository;
    private final PlayerStatRepository playerStatRepository;
    private final SeasonService seasonService;

    @Cacheable(cacheNames = "players")
    public List<Player> getAllPlayers(boolean activeOnly) {

        if (!activeOnly) {
            return playerRepository.findAll();
        }

        return seasonService.getActiveSeason()
                .map(season -> playerRepository.findByCurrentSeasonName(season.getSeasonName()))
                .orElse(List.of());

    }

    @Transactional(readOnly = true)
    public PlayerRankingDto.Response getPlayerRankings(String position, int page, int pageSize) {

        Optional<Season> rankingSeason = seasonService.getRankingSeason();

        if (rankingSeason.isEmpty()) {
            return PlayerRankingDto.Response.builder()
                    .rows(List.of())
                    .hasMore(false)
                    .tallying(true)
                    .seasonLabel(null)
                    .build();
        }

        String seasonName = rankingSeason.get().getSeasonName();

        int safePage = Math.max(page, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), MAX_PAGE_SIZE);

        Pageable pageable = PageRequest.of(safePage - 1, safePageSize);

        Page<PlayerStatRepository.PlayerRankingRow> resultPage = playerStatRepository.findPlayerRankings(seasonName, position, pageable);
        List<PlayerStatRepository.PlayerRankingRow> content = resultPage.getContent();

        int startRank = (safePage - 1) * safePageSize + 1;

        List<PlayerRankingDto.Row> rows = new ArrayList<>();

        for (int i = 0; i < content.size(); i++) {
            PlayerStatRepository.PlayerRankingRow row = content.get(i);
            Player player = row.getPlayer();

            rows.add(PlayerRankingDto.Row.builder()
                    .rank(startRank + i)
                    .playerId(player.getPlayerId())
                    .name(player.getPlayerName())
                    .team(player.getTeamName())
                    .pos(player.getPosition())
                    .score(row.getTotalScore())
                    .build());
        }

        return PlayerRankingDto.Response.builder()
                .rows(rows)
                .hasMore(resultPage.hasNext())
                .tallying(rows.isEmpty())
                .seasonLabel(formatSeasonLabel(seasonName))
                .build();

    }

    private String formatSeasonLabel(String seasonName) {
        return seasonName.replace("/", " · ").replace(" Season", "");
    }

}
