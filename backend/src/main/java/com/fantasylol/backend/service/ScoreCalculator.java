package com.fantasylol.backend.service;

import com.fantasylol.backend.entity.PlayerStat;
import org.springframework.stereotype.Component;

@Component
public class ScoreCalculator {

    public double calculateActualScore(PlayerStat stat) {

        int winBonus = Boolean.TRUE.equals(stat.getPlayerWin()) ? 1 : 0;

        return stat.getKills() * 3.0
                + stat.getAssists() * 1.0
                - stat.getDeaths() * 1.0
                + winBonus * 5.0
                + stat.getCs() * 0.01
                + stat.getDamageToChampions() * 0.001
                + stat.getVisionScore() * 0.2;

    }

}
