package com.ahmadisyraf39.spabs_v2.membership.repository;

import com.ahmadisyraf39.spabs_v2.membership.entity.PlayerMembership;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerMembershipRepository extends JpaRepository<PlayerMembership, Long> {

    List<PlayerMembership> findByPlayerId(Long playerId);

    Optional<PlayerMembership> findByPlayerIdAndLeftAtIsNull(Long playerId);
}
