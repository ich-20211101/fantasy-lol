package com.fantasylol.backend.service;

import com.fantasylol.backend.dto.PlayerPurchaseDto;
import com.fantasylol.backend.dto.PlayerRankingDto;
import com.fantasylol.backend.entity.Player;
import com.fantasylol.backend.entity.Season;
import com.fantasylol.backend.repository.PlayerRepository;
import com.fantasylol.backend.repository.PlayerStatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
                .map(season -> playerRepository.findByCurrentSeasonNameAndStatus(season.getSeasonName(), "CURRENT"))
                .orElse(List.of());

    }

    @CacheEvict(cacheNames = "players", allEntries = true)
    @Transactional
    public void updatePlayerStatus(Long playerId, String status) {

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 선수입니다: " + playerId));

        player.setStatus(status);
        playerRepository.save(player);

        log.info("Player status updated: {} ({}) -> {}", player.getPlayerName(), playerId, status);

    }

    @Cacheable(cacheNames = "playerRankings")
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

        Season season = rankingSeason.get();
        String seasonName = season.getSeasonName();

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
                    .score(row.getAvgScore())
                    .matchesPlayed(row.getMatchesPlayed())
                    .build());
        }

        return PlayerRankingDto.Response.builder()
                .rows(rows)
                .hasMore(resultPage.hasNext())
                .tallying(rows.isEmpty())
                .seasonLabel(formatSeasonLabel(seasonName))
                .build();

    }

    // 로스터 구매 기준 시즌을 바꿀 때 호출 — 그 시즌에 실제로 뛴 선수는 CURRENT로, 나머지(다른 팀 소속이거나 이미 이적한 선수)는 전부 DEPARTED로 일괄 전환
    @CacheEvict(cacheNames = "players", allEntries = true)
    @Transactional
    public void syncStatusForRosterSourceSeason(String seasonName) {

        List<PlayerStatRepository.PlayerSeasonAggregate> aggregates = playerStatRepository.findSeasonAggregates(seasonName);

        if (aggregates.isEmpty()) {
            throw new IllegalArgumentException("해당 시즌에 동기화된 경기 데이터가 없습니다. 먼저 매치를 동기화한 뒤 다시 시도해주세요: " + seasonName);
        }

        Set<Long> participantIds = aggregates.stream()
                .map(a -> a.getPlayer().getPlayerId())
                .collect(Collectors.toSet());

        List<Player> allPlayers = playerRepository.findAll();
        int departed = 0;
        int current = 0;

        for (Player player : allPlayers) {

            String newStatus = participantIds.contains(player.getPlayerId()) ? "CURRENT" : "DEPARTED";

            if (!newStatus.equals(player.getStatus())) {
                player.setStatus(newStatus);
                playerRepository.save(player);
            }

            if (newStatus.equals("CURRENT")) current++; else departed++;

        }

        log.info("Roster status synced for reference season {}: {} current, {} departed", seasonName, current, departed);

    }

    @Transactional(readOnly = true)
    public PlayerPurchaseDto.Response getPurchaseList() {

        Optional<Season> rosterSourceSeason = seasonService.getRosterSourceSeason();

        if (rosterSourceSeason.isEmpty()) {
            return PlayerPurchaseDto.Response.builder().rows(List.of()).sourceSeasonName(null).sourceSeasonLabel(null).build();
        }

        String seasonName = rosterSourceSeason.get().getSeasonName();

        List<PlayerPurchaseDto.Row> rows = playerStatRepository.findSeasonAggregates(seasonName).stream()
                .filter(a -> "CURRENT".equals(a.getPlayer().getStatus()))
                .map(a -> {
                    Player p = a.getPlayer();
                    return PlayerPurchaseDto.Row.builder()
                            .playerId(p.getPlayerId())
                            .name(p.getPlayerName())
                            .team(p.getTeamName())
                            .pos(p.getPosition())
                            .price(p.getPrice())
                            .priceInsufficientData(p.getPriceInsufficientData())
                            .score(a.getAvgScore())
                            .build();
                })
                .toList();

        return PlayerPurchaseDto.Response.builder()
                .rows(rows)
                .sourceSeasonName(seasonName)
                .sourceSeasonLabel(formatSeasonLabel(seasonName))
                .build();

    }

    private String formatSeasonLabel(String seasonName) {
        if (seasonName == null) return null;
        return seasonName.replace("/", " · ").replace(" Season", "");
    }

}
