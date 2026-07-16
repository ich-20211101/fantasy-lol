package com.fantasylol.backend.service;

import com.fantasylol.backend.entity.Team;
import com.fantasylol.backend.entity.TeamRoster;
import com.fantasylol.backend.entity.WeeklyStarter;
import com.fantasylol.backend.repository.TeamRepository;
import com.fantasylol.backend.repository.TeamRosterRepository;
import com.fantasylol.backend.repository.WeeklyStarterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklyStarterService {

    private final TeamRepository teamRepository;
    private final TeamRosterRepository teamRosterRepository;
    private final WeeklyStarterRepository weeklyStarterRepository;

    @Transactional
    public int lockStartersForWeek(Integer weekNumber, String seasonName) {

        List<Team> teams = teamRepository.findAll();
        int lockedCount = 0;

        for (Team team : teams) {

            if (weeklyStarterRepository.existsByTeamTeamIdAndWeekNumberAndSeasonName(team.getTeamId(), weekNumber, seasonName)) {
                log.info("Already locked, skipping team {}: week {} {}", team.getTeamId(), weekNumber, seasonName);
                continue;
            }

            List<TeamRoster> starters = teamRosterRepository.findByTeamTeamId(team.getTeamId()).stream()
                    .filter(r -> Boolean.TRUE.equals(r.getIsStarter())).toList();

            if (starters.isEmpty()) {
                log.info("No starters set, skipping team {}: week {} {}", team.getTeamId(), weekNumber, seasonName);
                continue;
            }

            for (TeamRoster roster : starters) {
                weeklyStarterRepository.save(WeeklyStarter.builder()
                        .team(team)
                        .player(roster.getPlayer())
                        .weekNumber(weekNumber)
                        .seasonName(seasonName)
                        .build());
            }

            lockedCount ++;

        }

        log.info("Locked starters for {} teams: week {} {}", lockedCount, weekNumber, seasonName);

        return lockedCount;

    }

}
