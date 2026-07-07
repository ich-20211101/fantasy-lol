package com.fantasylol.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_scores")
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class UserScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userScoreId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer weekNumber;

    @Column(nullable = false)
    private String seasonName;

    @Column(nullable = false)
    @Builder.Default
    private Double weeklyScore = 0.0;

    @Column(nullable = false)
    @Builder.Default
    private Double seasonalScore = 0.0;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
