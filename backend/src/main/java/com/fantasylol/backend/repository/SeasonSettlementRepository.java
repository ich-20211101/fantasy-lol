package com.fantasylol.backend.repository;

import com.fantasylol.backend.entity.SeasonSettlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SeasonSettlementRepository extends JpaRepository<SeasonSettlement, Long> {

    @Query("SELECT CASE WHEN COUNT(ss) > 0 THEN true ELSE false END FROM SeasonSettlement ss WHERE ss.season.seasonName = :seasonName")
    boolean existsBySeasonName(@Param("seasonName") String seasonName);

    @Query("SELECT ss FROM SeasonSettlement ss WHERE ss.season.seasonName = :seasonName ORDER BY ss.rank ASC")
    List<SeasonSettlement> findBySeasonNameOrderByRankAsc(@Param("seasonName") String seasonName);

    List<SeasonSettlement> findByUserUserId(Long userId);

}
