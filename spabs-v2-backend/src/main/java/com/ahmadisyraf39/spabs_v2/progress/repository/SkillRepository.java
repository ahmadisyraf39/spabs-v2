package com.ahmadisyraf39.spabs_v2.progress.repository;

import com.ahmadisyraf39.spabs_v2.progress.entity.Skill;
import com.ahmadisyraf39.spabs_v2.team.entity.enums.AgeGroup;
import com.ahmadisyraf39.spabs_v2.team.entity.enums.TeamCategory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillRepository extends JpaRepository<Skill, Long> {

    List<Skill> findByAgeGroupAndCategory(AgeGroup ageGroup, TeamCategory category);
}
