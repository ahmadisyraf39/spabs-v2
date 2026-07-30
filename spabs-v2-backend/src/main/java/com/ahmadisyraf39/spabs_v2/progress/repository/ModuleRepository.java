package com.ahmadisyraf39.spabs_v2.progress.repository;

import com.ahmadisyraf39.spabs_v2.progress.entity.Module;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModuleRepository extends JpaRepository<Module, Long> {

    List<Module> findBySkillId(Long skillId);
}
