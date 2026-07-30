package com.ahmadisyraf39.spabs_v2.player.repository;

import com.ahmadisyraf39.spabs_v2.player.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepository extends JpaRepository<Player, Long> {
}
