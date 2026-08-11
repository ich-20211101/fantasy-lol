package com.fantasylol.backend.service;

import com.fantasylol.backend.entity.Player;
import com.fantasylol.backend.repository.PlayerRepository;
import com.fantasylol.backend.repository.PlayerStatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerPricingService {

    private static final int MIN_MATCHES = 5;
    private static final double MIN_PRICE = 5.0;
    private static final double MAX_PRICE = 25.0;

    private final PlayerStatRepository playerStatRepository;
    private final PlayerRepository playerRepository;

    @CacheEvict(cacheNames = "players", allEntries = true)
    @Transactional
    public void calculatePricesForSeason(String seasonName) {

        List<PlayerStatRepository.PlayerSeasonAggregate> aggregates = playerStatRepository.findSeasonAggregates(seasonName);

        List<PlayerStatRepository.PlayerSeasonAggregate> qualified = aggregates.stream()
                .filter(a -> a.getMatchesPlayed() >= MIN_MATCHES)
                .sorted(Comparator.comparing(PlayerStatRepository.PlayerSeasonAggregate::getAvgScore))
                .toList();

        int n = qualified.size();

        for (int i = 0; i < n; i ++) {
            double percentile = n > 1 ? (double) i / (n - 1) : 0.5;
            double price = roundToOneDecimal(MIN_PRICE + percentile * (MAX_PRICE - MIN_PRICE));

            Player player = qualified.get(i).getPlayer();
            player.setPrice(price);
            player.setPriceInsufficientData(false);
            playerRepository.save(player);
        }

        List<Player> unqualified = aggregates.stream()
                .filter(a -> a.getMatchesPlayed() < MIN_MATCHES)
                .map(PlayerStatRepository.PlayerSeasonAggregate::getPlayer)
                .toList();

        for (Player player : unqualified) {
            player.setPrice(MIN_PRICE);
            player.setPriceInsufficientData(true);
            playerRepository.save(player);
        }

        log.info("Priced season {}: {} qualified players scaled to {}~{}, {} players below {}-match minimum fixed at {}", seasonName, n, MIN_PRICE, MAX_PRICE, unqualified.size(), MIN_MATCHES, MIN_PRICE);

    }

    private double roundToOneDecimal(double value) {
        return Math.round(value * 10) / 10.0;
    }

}
