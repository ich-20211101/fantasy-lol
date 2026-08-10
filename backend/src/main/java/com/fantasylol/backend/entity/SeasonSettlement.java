package com.fantasylol.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "season_settlements", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "season_id"}))
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class SeasonSettlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seasonSettlementId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id", nullable = false)
    private Season season;

    @Column(nullable = false)
    private Double totalScore;

    @Column(nullable = false)
    private Integer rank;

    private LocalDateTime settledAt;

    @PrePersist
    protected void onCreate() {
        settledAt = LocalDateTime.now();
    }

}
