package com.fantasylol.backend.service;

import com.fantasylol.backend.dto.TeamDto;
import com.fantasylol.backend.entity.Player;
import com.fantasylol.backend.entity.Team;
import com.fantasylol.backend.entity.TeamRoster;
import com.fantasylol.backend.entity.User;
import com.fantasylol.backend.repository.PlayerRepository;
import com.fantasylol.backend.repository.TeamRepository;
import com.fantasylol.backend.repository.TeamRosterRepository;
import com.fantasylol.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamService {

    private static final int ROSTER_SIZE = 8;
    private static final int STARTER_SIZE = 5;
    private static final Set<String> POSITIONS = Set.of("Top", "Jungle", "Mid", "Bot", "Support");

    private final TeamRepository teamRepository;
    private final TeamRosterRepository teamRosterRepository;
    private final PlayerRepository playerRepository;
    private final UserRepository userRepository;

    @Transactional
    public TeamDto.Response createTeam(OAuth2User oAuth2User, TeamDto.CreateRequest request) {

        User user = getUser(oAuth2User);

        teamRepository.findByUserUserId(user.getUserId()).ifPresent(t -> {
            throw new IllegalStateException("Team already exists");
        });

        Team team = teamRepository.save(Team.builder()
                .user(user)
                .teamName(request.getTeamName())
                .build());

        return toResponse(team, List.of());

    }

    @Transactional
    public TeamDto.Response submitRoster(OAuth2User oAuth2User, Long teamId, TeamDto.RosterSubmitRequest request) {

        User user = getUser(oAuth2User);
        Team team = getTeamAndValidateOwner(teamId, user);

        if (team.getRosterLocked()) {
            throw new IllegalStateException("Team is already locked");
        }

        List<Long> playerIds = request.getPlayerIds();

        if (playerIds == null || playerIds.size() != ROSTER_SIZE) {
            throw new IllegalArgumentException("Select exactly 8 players");
        }

        if (playerIds.stream().distinct().count() != ROSTER_SIZE) {
            throw new IllegalArgumentException("Duplicate players detected");
        }

        List<Player> players = playerRepository.findAllById(playerIds);

        if (players.size() != ROSTER_SIZE) {
            throw new IllegalArgumentException("Invalid player selected");
        }

        Map<String, List<Player>> byPosition = players.stream()
                .collect(Collectors.groupingBy(Player::getPosition));

        for (String pos : POSITIONS) {
            if (!byPosition.containsKey(pos)) {
                throw new IllegalArgumentException(pos + " player not found");
            }
        }

        Map<String, Boolean> starterByPosition = POSITIONS.stream()
                .collect(Collectors.toMap(Function.identity(), pos -> true));

        List<TeamRoster> rosterEntries = players.stream()
                .map(player -> {
                    boolean isStarter = starterByPosition.getOrDefault(player.getPosition(), false);

                    if (isStarter) {
                        starterByPosition.put(player.getPosition(), false);
                    }

                    return TeamRoster.builder()
                            .team(team)
                            .player(player)
                            .isStarter(isStarter)
                            .build();

                })
                .collect(Collectors.toList());

        teamRosterRepository.saveAll(rosterEntries);

        team.setRosterLocked(true);
        teamRepository.save(team);

        return toResponse(team, rosterEntries);

    }

    @Transactional
    public TeamDto.Response updateStarters(OAuth2User oAuth2User, Long teamId, TeamDto.StarterUpdateRequest request) {

        User user = getUser(oAuth2User);
        Team team = getTeamAndValidateOwner(teamId, user);

        if (!team.getRosterLocked()) {
            throw new IllegalStateException("Roster not locked");
        }

        List<Long> starterPlayerIds = request.getPlayerIds();

        if (starterPlayerIds == null || starterPlayerIds.size() != STARTER_SIZE) {
            throw new IllegalArgumentException("Select exactly 5 players");
        }

        List<TeamRoster> roster = teamRosterRepository.findByTeamTeamId(teamId);

        Set<Long> rosterPlayerIds = roster.stream()
                .map(r -> r.getPlayer().getPlayerId())
                .collect(Collectors.toSet());

        for (Long pid : starterPlayerIds) {
            if (!rosterPlayerIds.contains(pid)) {
                throw new IllegalArgumentException("Player not found");
            }
        }

        List<Player> starterPlayers = roster.stream()
                .filter(r -> starterPlayerIds.contains(r.getPlayer().getPlayerId()))
                .map(TeamRoster::getPlayer)
                .collect(Collectors.toList());

        Map<String, Long> positionCount = starterPlayers.stream()
                .collect(Collectors.groupingBy(Player::getPosition, Collectors.counting()));

        for (String pos : POSITIONS) {
            if (!positionCount.containsKey(pos) || positionCount.get(pos) != 1) {
                throw new IllegalArgumentException(pos + " player not found");
            }
        }

        roster.forEach(r -> {
            r.setIsStarter(starterPlayerIds.contains(r.getPlayer().getPlayerId()));
            teamRosterRepository.save(r);
        });

        return toResponse(team, roster);

    }

    @Transactional
    public TeamDto.Response getTeam(OAuth2User oAuth2User, Long teamId) {

        User user = getUser(oAuth2User);
        Team team = getTeamAndValidateOwner(teamId, user);
        List<TeamRoster> roster = teamRosterRepository.findByTeamTeamId(teamId);

        return toResponse(team, roster);

    }

    private User getUser(OAuth2User oAuth2User) {

        String email = oAuth2User.getAttribute("email");

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

    }

    private Team getTeamAndValidateOwner(Long teamId, User user) {

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));

        if (!team.getUser().getUserId().equals(user.getUserId())) {
            throw new IllegalStateException("You can only edit your own team");
        }

        return team;

    }

    private TeamDto.Response toResponse(Team team, List<TeamRoster> roster) {

        List<TeamDto.RosterPlayerResponse> rosterResponses = roster.stream()
                .map(r -> TeamDto.RosterPlayerResponse.builder()
                        .teamRosterId(r.getTeamRosterId())
                        .playerId(r.getPlayer().getPlayerId())
                        .playerName(r.getPlayer().getPlayerName())
                        .position(r.getPlayer().getPosition())
                        .teamName(r.getPlayer().getTeamName())
                        .isStarter(r.getIsStarter())
                        .build())
                .toList();

        return TeamDto.Response.builder()
                .teamId(team.getTeamId())
                .teamName(team.getTeamName())
                .roster(rosterResponses)
                .build();

    }


}
