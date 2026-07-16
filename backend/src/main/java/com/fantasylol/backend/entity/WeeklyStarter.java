package com.fantasylol.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "weekly_starters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeeklyStarter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long weeklyStarterId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(nullable = false)
    private Integer weekNumber;

    @Column(nullable = false)
    private String seasonName;

    private LocalDateTime lockedAt;

    @PrePersist
    protected void onCreate() {
        lockedAt = LocalDateTime.now();
    }

}
