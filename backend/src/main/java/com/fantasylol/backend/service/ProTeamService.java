package com.fantasylol.backend.service;

import com.fantasylol.backend.dto.ProTeamDto;
import com.fantasylol.backend.entity.ProTeam;
import com.fantasylol.backend.repository.PlayerRepository;
import com.fantasylol.backend.repository.ProTeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProTeamService {

    private final ProTeamRepository proTeamRepository;
    private final PlayerRepository playerRepository;

    @Transactional(readOnly = true)
    public List<ProTeamDto.Response> getAllProTeams() {
        return proTeamRepository.findAllByOrderByFullNameAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public int syncFromPlayers() {

        int created = 0;

        for (String teamName : playerRepository.findDistinctTeamNames()) {
            if (!proTeamRepository.existsByFullName(teamName)) {
                proTeamRepository.save(ProTeam.builder()
                        .fullName(teamName)
                        .shortName(teamName)
                        .status("CURRENT")
                        .build());
                created++;
            }
        }

        return created;

    }

    @Transactional
    public ProTeamDto.Response updateProTeam(Long proTeamId, ProTeamDto.UpdateRequest request) {

        ProTeam proTeam = proTeamRepository.findById(proTeamId)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다: " + proTeamId));

        if (request.getShortName() != null) proTeam.setShortName(request.getShortName());
        if (request.getStatus() != null) proTeam.setStatus(request.getStatus());

        return toResponse(proTeam);

    }

    @Transactional
    public void deleteProTeam(Long proTeamId) {
        proTeamRepository.deleteById(proTeamId);
    }

    private ProTeamDto.Response toResponse(ProTeam proTeam) {
        return ProTeamDto.Response.builder()
                .proTeamId(proTeam.getProTeamId())
                .fullName(proTeam.getFullName())
                .shortName(proTeam.getShortName())
                .status(proTeam.getStatus())
                .build();
    }

}
