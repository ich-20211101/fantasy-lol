package com.fantasylol.backend.service;

import com.fantasylol.backend.entity.Season;
import com.fantasylol.backend.entity.Team;
import com.fantasylol.backend.entity.TeamRoster;
import com.fantasylol.backend.entity.WeeklyStarter;
import com.fantasylol.backend.repository.TeamRepository;
import com.fantasylol.backend.repository.TeamRosterRepository;
import com.fantasylol.backend.repository.WeeklyStarterRepository;
import com.fantasylol.backend.util.KstTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklyStarterService {

    private final TeamRepository teamRepository;
    private final TeamRosterRepository teamRosterRepository;
    private final WeeklyStarterRepository weeklyStarterRepository;
    private final SeasonService seasonService;

    @Transactional
    public int lockStartersForDate(LocalDate date, String seasonName) {

        Season season = seasonService.getBySeasonName(seasonName)
                .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 시즌입니다: " + seasonName));

        int weekNumber = seasonService.resolveWeekNumber(seasonName, date);

        return lockStartersForWeek(weekNumber, season);

    }

    private int lockStartersForWeek(Integer weekNumber, Season season) {

        List<Team> teams = teamRepository.findAll();
        int lockedCount = 0;

        for (Team team : teams) {

            if (weeklyStarterRepository.existsByTeamTeamIdAndWeekNumberAndSeasonSeasonId(team.getTeamId(), weekNumber, season.getSeasonId())) {
                log.info("Already locked, skipping team {}: week {} {}", team.getTeamId(), weekNumber, season.getSeasonName());
                continue;
            }

            List<TeamRoster> starters = teamRosterRepository.findByTeamTeamId(team.getTeamId()).stream()
                    .filter(r -> Boolean.TRUE.equals(r.getIsStarter())).toList();

            if (starters.isEmpty()) {
                log.info("No starters set, skipping team {}: week {} {}", team.getTeamId(), weekNumber, season.getSeasonName());
                continue;
            }

            for (TeamRoster roster : starters) {
                weeklyStarterRepository.save(WeeklyStarter.builder()
                        .team(team)
                        .player(roster.getPlayer())
                        .weekNumber(weekNumber)
                        .season(season)
                        .build());
            }

            lockedCount ++;

        }

        log.info("Locked starters for {} teams: week {} {}", lockedCount, weekNumber, season.getSeasonName());

        return lockedCount;

    }

    @Transactional(readOnly = true)
    public boolean isCurrentWeekLockedForTeam(Team team) {

        String seasonName = team.getCurrentSeasonName();
        if (seasonName == null) return false;

        Season season = seasonService.getBySeasonName(seasonName).orElse(null);
        if (season == null) return false;

        int weekNumber = seasonService.resolveWeekNumber(seasonName, KstTime.nowKstDate());

        return weeklyStarterRepository.existsByTeamTeamIdAndWeekNumberAndSeasonSeasonId(team.getTeamId(), weekNumber, season.getSeasonId());

    }

}
