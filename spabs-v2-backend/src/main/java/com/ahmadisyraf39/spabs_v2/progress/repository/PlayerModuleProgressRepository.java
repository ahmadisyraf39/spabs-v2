package com.ahmadisyraf39.spabs_v2.progress.repository;

import com.ahmadisyraf39.spabs_v2.progress.entity.PlayerModuleProgress;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerModuleProgressRepository extends JpaRepository<PlayerModuleProgress, Long> {

    List<PlayerModuleProgress> findByPlayerId(Long playerId);

    List<PlayerModuleProgress> findByModuleId(Long moduleId);

    Optional<PlayerModuleProgress> findByModuleIdAndPlayerId(Long moduleId, Long playerId);
}
