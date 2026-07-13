package com.fantasylol.backend.repository;

import com.fantasylol.backend.entity.WithdrawalFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WithdrawalFeedbackRepository extends JpaRepository<WithdrawalFeedback, Long> {
}
