package com.fantasylol.backend.repository;

import com.fantasylol.backend.entity.SeasonSettlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeasonSettlementRepository extends JpaRepository<SeasonSettlement, Long> {

    boolean existsBySeasonName(String seasonName);

    List<SeasonSettlement> findBySeasonNameOrderByRankAsc(String seasonName);

}
