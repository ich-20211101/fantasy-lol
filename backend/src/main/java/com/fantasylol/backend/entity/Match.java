package com.fantasylol.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "matches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long matchId;

    @Column(unique = true)
    private String leaguepediaMatchId;

    private String seasonName;

    @Column(nullable = false)
    private String team1;

    @Column(nullable = false)
    private String team2;

    @Column(nullable = false)
    private LocalDateTime matchDate;

    private Integer bestOf;

    @Column(nullable = false)
    @Builder.Default
    private String status = "SCHEDULED";

    private String winner;

}
