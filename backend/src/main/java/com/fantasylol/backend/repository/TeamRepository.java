package com.fantasylol.backend.repository;

import com.fantasylol.backend.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {

    List<Team> findByUserUserId(Long userId);

    Optional<Team> findByUserUserIdAndSeasonSeasonId(Long userId, Long seasonId);

    List<Team> findByUserUserIdIn(Collection<Long> userIds);

    List<Team> findByUserUserIdInAndSeasonSeasonName(Collection<Long> userIds, String seasonName);

    List<Team> findBySeasonSeasonId(Long seasonId);

    List<Team> findBySeasonSeasonName(String seasonName);

}
