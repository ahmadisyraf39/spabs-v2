package com.ahmadisyraf39.spabs_v2.membership.repository;

import com.ahmadisyraf39.spabs_v2.membership.entity.CoachMembership;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoachMembershipRepository extends JpaRepository<CoachMembership, Long> {

    List<CoachMembership> findByCoachId(Long coachId);

    Optional<CoachMembership> findByCoachIdAndLeftAtIsNull(Long coachId);
}
