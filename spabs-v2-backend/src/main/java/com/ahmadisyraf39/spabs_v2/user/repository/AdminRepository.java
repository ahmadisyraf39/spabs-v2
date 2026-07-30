package com.ahmadisyraf39.spabs_v2.user.repository;

import com.ahmadisyraf39.spabs_v2.user.entity.Admin;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findByUserId(Long userId);
}
