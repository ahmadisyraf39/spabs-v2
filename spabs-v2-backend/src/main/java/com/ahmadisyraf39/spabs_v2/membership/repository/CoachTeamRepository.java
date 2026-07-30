package com.ahmadisyraf39.spabs_v2.membership.repository;

import com.ahmadisyraf39.spabs_v2.membership.entity.CoachTeam;
import com.ahmadisyraf39.spabs_v2.membership.entity.enums.CoachTeamStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoachTeamRepository extends JpaRepository<CoachTeam, Long> {

    List<CoachTeam> findByCoachId(Long coachId);

    List<CoachTeam> findByTeamId(Long teamId);

    List<CoachTeam> findByTeamIdAndStatus(Long teamId, CoachTeamStatus status);
}
