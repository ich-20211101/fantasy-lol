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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamService {

    private static final int ROSTER_SIZE = 8;
    private static final int STARTER_SIZE = 5;
    private static final Set<String> REQUIRED_POSITIONS = Set.of("Top", "Jungle", "Mid", "Bot", "Support");

    private final TeamRepository teamRepository;
    private final TeamRosterRepository teamRosterRepository;
    private final PlayerRepository playerRepository;
    private final UserRepository userRepository;

    @Transactional
    public TeamDto.Response submitRoster(OAuth2User oAuth2User, TeamDto.RosterSubmitRequest request) {

        User user = getUser(oAuth2User);
        List<Player> players = validateAndFetchRosterPlayers(request.getPlayerIds());
        Team team = teamRepository.findByUserUserId(user.getUserId())
                .orElseGet(() -> Team.builder().user(user).build());

        if (Boolean.TRUE.equals(team.getRosterLocked())) {
            throw new IllegalStateException("시즌이 시작되어 로스터를 수정할 수 없습니다");
        }

        team.setTeamName(request.getTeamName());
        Team savedTeam = teamRepository.save(team);

        replaceRoster(savedTeam, players);

        List<TeamRoster> roster = teamRosterRepository.findByTeamTeamId(savedTeam.getTeamId());

        return toResponse(savedTeam, roster);

    }

    @Transactional
    public TeamDto.Response updateStarters(OAuth2User oAuth2User, Long teamId, TeamDto.StarterUpdateRequest request) {

        User user = getUser(oAuth2User);
        Team team = getOwnedTeam(teamId, user);
        List<TeamRoster> roster = teamRosterRepository.findByTeamTeamId(teamId);

        if (roster.isEmpty()) {
            throw new IllegalStateException("등록된 로스터가 없습니다");
        }

        Set<Long> starterIds = validateStarterSelection(request.getPlayerIds(), roster);

        roster.forEach(r -> r.setIsStarter(starterIds.contains(r.getPlayer().getPlayerId())));
        teamRosterRepository.saveAll(roster);

        return toResponse(team, roster);

    }

    @Transactional(readOnly = true)
    public TeamDto.Response getMyTeam(OAuth2User oAuth2User) {

        User user = getUser(oAuth2User);

        return teamRepository.findByUserUserId(user.getUserId())
                .map(team -> toResponse(team, teamRosterRepository.findByTeamTeamId(team.getTeamId())))
                .orElse(null);

    }

    @Transactional(readOnly = true)
    public List<TeamDto.RosterPlayerResponse> getStarters(OAuth2User oAuth2User) {

        User user = getUser(oAuth2User);
        Team team = teamRepository.findByUserUserId(user.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다."));

        return teamRosterRepository.findByTeamTeamId(team.getTeamId()).stream()
                .filter(r -> Boolean.TRUE.equals(r.getIsStarter()))
                .map(r -> TeamDto.RosterPlayerResponse.builder()
                        .teamRosterId(r.getTeamRosterId())
                        .playerId(r.getPlayer().getPlayerId())
                        .playerName(r.getPlayer().getPlayerName())
                        .position(r.getPlayer().getPosition())
                        .teamName(r.getPlayer().getTeamName())
                        .isStarter(r.getIsStarter())
                        .build())
                .toList();

    }

    private List<Player> validateAndFetchRosterPlayers(List<Long> playerIds) {

        if (playerIds == null || playerIds.size() != ROSTER_SIZE) {
            throw new IllegalArgumentException("선수는 정확히 " + ROSTER_SIZE + "명이어야 합니다.");
        }

        Set<Long> uniqueIds = new LinkedHashSet<>(playerIds);
        if (uniqueIds.size() != ROSTER_SIZE) {
            throw new IllegalArgumentException("중복된 선수가 포함되어 있습니다.");
        }

        List<Player> players = playerRepository.findAllById(uniqueIds);
        if (players.size() != ROSTER_SIZE) {
            throw new IllegalArgumentException("존재하지 않는 선수가 포함되어 있습니다.");
        }

        Set<String> coveredPositions = players.stream()
                .map(Player::getPosition)
                .collect(Collectors.toSet());

        Set<String> missing = REQUIRED_POSITIONS.stream()
                .filter(pos -> !coveredPositions.contains(pos))
                .collect(Collectors.toSet());

        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("다음 포지션 선수가 없습니다: " + String.join(", ", missing));
        }

        return players;

    }

    private Set<Long> validateStarterSelection(List<Long> starterPlayerIds, List<TeamRoster> roster) {

        if (starterPlayerIds == null || starterPlayerIds.size() != STARTER_SIZE) {
            throw new IllegalArgumentException("스타터는 정확히 " + STARTER_SIZE + "명이어야 합니다.");
        }

        Map<Long, Player> rosterPlayersById = roster.stream()
                .collect(Collectors.toMap(r -> r.getPlayer().getPlayerId(), TeamRoster::getPlayer));

        Set<Long> starterIds = new LinkedHashSet<>(starterPlayerIds);
        if (starterIds.size() != STARTER_SIZE) {
            throw new IllegalArgumentException("중복된 선수가 포함되어 있습니다.");
        }

        for (Long id : starterIds) {
            if (!rosterPlayersById.containsKey(id)) {
                throw new IllegalArgumentException("로스터에 없는 선수입니다: " + id);
            }
        }

        Map<String, Long> positionCounts = starterIds.stream()
                .map(rosterPlayersById::get)
                .collect(Collectors.groupingBy(Player::getPosition, Collectors.counting()));

        for (String pos : REQUIRED_POSITIONS) {
            if (positionCounts.getOrDefault(pos, 0L) != 1L) {
                throw new IllegalArgumentException(pos + " 포지션 스타터가 정확히 1명이어야 합니다.");
            }
        }

        return starterIds;

    }

    private void replaceRoster(Team team, List<Player> players) {

        List<TeamRoster> existing = teamRosterRepository.findByTeamTeamId(team.getTeamId());
        if (!existing.isEmpty()) {
            teamRosterRepository.deleteAll(existing);
        }

        Set<String> assignedStarterPositions = new LinkedHashSet<>();

        List<TeamRoster> newRoster = players.stream()
                .map(player -> {
                    boolean isStarter = assignedStarterPositions.add(player.getPosition());
                    return TeamRoster.builder()
                            .team(team)
                            .player(player)
                            .isStarter(isStarter)
                            .build();
                })
                .collect(Collectors.toList());

        teamRosterRepository.saveAll(newRoster);

    }

    private User getUser(OAuth2User oAuth2User) {

        String email = oAuth2User.getAttribute("email");

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

    }

    private Team getOwnedTeam(Long teamId, User user) {

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다."));

        if (!team.getUser().getUserId().equals(user.getUserId())) {
            throw new IllegalStateException("본인의 팀만 수정할 수 있습니다.");
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
                .rosterLocked(Boolean.TRUE.equals(team.getRosterLocked()))
                .roster(rosterResponses)
                .build();

    }

    @Transactional
    public void deleteMyTeam(OAuth2User oAuth2User) {
        User user = getUser(oAuth2User);
        Team team = teamRepository.findByUserUserId(user.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다."));

        teamRosterRepository.deleteAll(teamRosterRepository.findByTeamTeamId(team.getTeamId()));
        teamRepository.delete(team);
    }

}
