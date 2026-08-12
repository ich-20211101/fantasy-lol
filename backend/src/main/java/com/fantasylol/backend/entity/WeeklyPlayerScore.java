package com.fantasylol.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "weekly_player_scores", uniqueConstraints = @UniqueConstraint(columnNames = {"team_id", "player_id", "season_id", "week_number"}))
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class WeeklyPlayerScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long weeklyPlayerScoreId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id", nullable = false)
    private Season season;

    @Column(nullable = false)
    private Integer weekNumber;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isStarter = false;

    @Column(nullable = false)
    @Builder.Default
    private Double score = 0.0;

    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onSave() {
        updatedAt = LocalDateTime.now();
    }

}
