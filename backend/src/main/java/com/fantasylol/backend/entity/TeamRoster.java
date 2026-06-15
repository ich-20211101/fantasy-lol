package com.fantasylol.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "team_roster")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamRoster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long teamRosterId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Builder.Default
    private Boolean isStarter = false;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

}
