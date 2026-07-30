package com.ahmadisyraf39.spabs_v2.membership.repository;

import com.ahmadisyraf39.spabs_v2.membership.entity.PlayerParent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerParentRepository extends JpaRepository<PlayerParent, Long> {

    List<PlayerParent> findByPlayerId(Long playerId);

    List<PlayerParent> findByParentId(Long parentId);
}
