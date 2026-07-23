package com.fantasylol.backend.service;

import com.fantasylol.backend.entity.Player;
import com.fantasylol.backend.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;
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

}
