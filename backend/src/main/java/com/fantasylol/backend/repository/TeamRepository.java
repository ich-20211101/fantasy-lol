package com.fantasylol.backend.repository;

import com.fantasylol.backend.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Team> findByUserUserId(Long userId);
}
