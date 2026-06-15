package com.fantasylol.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "player_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long playerStatId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @Column(nullable = false)
    private Integer gameNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(unique = true)
    private String leaguepediaGameId;

    private String team;
    private String role;
    private String champion;

    @Builder.Default private Integer kills = 0;
    @Builder.Default private Integer deaths = 0;
    @Builder.Default private Integer assists = 0;
    @Builder.Default private Integer gold = 0;
    @Builder.Default private Integer cs = 0;
    @Builder.Default private Integer damageToChampions = 0;
    @Builder.Default private Integer visionScore = 0;

    @Builder.Default private Boolean playerWin = false;
    @Builder.Default private Double actualScore = 0.0;

}
