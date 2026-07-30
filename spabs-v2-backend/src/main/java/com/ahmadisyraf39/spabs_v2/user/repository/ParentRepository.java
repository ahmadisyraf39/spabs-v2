package com.ahmadisyraf39.spabs_v2.user.repository;

import com.ahmadisyraf39.spabs_v2.user.entity.Parent;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParentRepository extends JpaRepository<Parent, Long> {

    Optional<Parent> findByUserId(Long userId);
}
