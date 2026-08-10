package com.fantasylol.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "weekly_settlements", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "season_id", "week_number"}))
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class WeeklySettlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long weeklySettlementId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id", nullable = false)
    private Season season;

    @Column(nullable = false)
    private Integer weekNumber;

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
