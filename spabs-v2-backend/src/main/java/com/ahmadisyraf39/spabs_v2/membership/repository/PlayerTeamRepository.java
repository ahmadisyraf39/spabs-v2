package com.ahmadisyraf39.spabs_v2.membership.repository;

import com.ahmadisyraf39.spabs_v2.membership.entity.PlayerTeam;
import com.ahmadisyraf39.spabs_v2.membership.entity.enums.PlayerTeamStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerTeamRepository extends JpaRepository<PlayerTeam, Long> {

    List<PlayerTeam> findByPlayerId(Long playerId);

    List<PlayerTeam> findByTeamId(Long teamId);

    List<PlayerTeam> findByTeamIdAndStatus(Long teamId, PlayerTeamStatus status);

    Optional<PlayerTeam> findByTeamIdAndJerseyNumberAndStatus(
            Long teamId, Integer jerseyNumber, PlayerTeamStatus status);
}
