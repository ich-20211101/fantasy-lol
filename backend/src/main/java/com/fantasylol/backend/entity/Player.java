package com.fantasylol.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "players")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long playerId;

    @Column(nullable = false)
    private String playerName;

    @Column(nullable = false)
    private String position;

    @Column(nullable = false)
    private String teamName;

    private String currentSeasonName;

}
