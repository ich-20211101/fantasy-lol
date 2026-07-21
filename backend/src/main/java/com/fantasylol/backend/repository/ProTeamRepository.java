package com.fantasylol.backend.repository;

import com.fantasylol.backend.entity.ProTeam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProTeamRepository extends JpaRepository<ProTeam, Long> {

    List<ProTeam> findAllByOrderByFullNameAsc();
    boolean existsByFullName(String fullName);

}