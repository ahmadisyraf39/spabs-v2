package com.ahmadisyraf39.spabs_v2.progress.service;

import com.ahmadisyraf39.spabs_v2.common.exception.ResourceNotFoundException;
import com.ahmadisyraf39.spabs_v2.progress.dto.request.SkillRequest;
import com.ahmadisyraf39.spabs_v2.progress.dto.response.SkillResponse;
import com.ahmadisyraf39.spabs_v2.progress.entity.Skill;
import com.ahmadisyraf39.spabs_v2.progress.mapper.SkillMapper;
import com.ahmadisyraf39.spabs_v2.progress.repository.SkillRepository;
import com.ahmadisyraf39.spabs_v2.team.entity.enums.AgeGroup;
import com.ahmadisyraf39.spabs_v2.team.entity.enums.TeamCategory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SkillService {

    private final SkillRepository skillRepository;
    private final SkillMapper skillMapper;

    public SkillResponse create(SkillRequest request) {
        Skill skill = skillMapper.toEntity(request);
        return skillMapper.toResponse(skillRepository.save(skill));
    }

    public SkillResponse getById(Long id) {
        return skillMapper.toResponse(findEntityById(id));
    }

    public List<SkillResponse> getAll() {
        return skillRepository.findAll().stream().map(skillMapper::toResponse).toList();
    }

    public List<SkillResponse> getByAgeGroupAndCategory(AgeGroup ageGroup, TeamCategory category) {
        return skillRepository.findByAgeGroupAndCategory(ageGroup, category).stream()
                .map(skillMapper::toResponse)
                .toList();
    }

    public SkillResponse update(Long id, SkillRequest request) {
        Skill skill = findEntityById(id);
        skillMapper.updateEntity(skill, request);
        return skillMapper.toResponse(skillRepository.save(skill));
    }

    public void delete(Long id) {
        skillRepository.delete(findEntityById(id));
    }

    private Skill findEntityById(Long id) {
        return skillRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found: " + id));
    }
}
