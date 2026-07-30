package com.ahmadisyraf39.spabs_v2.finance.repository;

import com.ahmadisyraf39.spabs_v2.finance.entity.CoachPayment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoachPaymentRepository extends JpaRepository<CoachPayment, Long> {

    List<CoachPayment> findByCoachId(Long coachId);
}
