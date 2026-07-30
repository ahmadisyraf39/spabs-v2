package com.ahmadisyraf39.spabs_v2.user.repository;

import com.ahmadisyraf39.spabs_v2.user.entity.Coach;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoachRepository extends JpaRepository<Coach, Long> {

    Optional<Coach> findByUserId(Long userId);
}
