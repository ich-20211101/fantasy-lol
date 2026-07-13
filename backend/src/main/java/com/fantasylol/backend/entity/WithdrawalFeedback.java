package com.fantasylol.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "withdrawal_feedbacks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WithdrawalFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long withdrawalFeedbackId;

    private Long userId;

    @Column(nullable = false)
    private String reason;

    private String note;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

}
