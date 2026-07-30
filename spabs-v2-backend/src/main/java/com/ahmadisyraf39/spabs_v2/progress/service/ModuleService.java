package com.ahmadisyraf39.spabs_v2.progress.service;

import com.ahmadisyraf39.spabs_v2.common.exception.ResourceNotFoundException;
import com.ahmadisyraf39.spabs_v2.progress.dto.request.ModuleRequest;
import com.ahmadisyraf39.spabs_v2.progress.dto.response.ModuleResponse;
import com.ahmadisyraf39.spabs_v2.progress.entity.Module;
import com.ahmadisyraf39.spabs_v2.progress.entity.Skill;
import com.ahmadisyraf39.spabs_v2.progress.mapper.ModuleMapper;
import com.ahmadisyraf39.spabs_v2.progress.repository.ModuleRepository;
import com.ahmadisyraf39.spabs_v2.progress.repository.SkillRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ModuleService {

    private final ModuleRepository moduleRepository;
    private final SkillRepository skillRepository;
    private final ModuleMapper moduleMapper;

    public ModuleResponse create(ModuleRequest request) {
        Module module = moduleMapper.toEntity(request);
        module.setSkill(findSkillById(request.getSkillId()));
        return moduleMapper.toResponse(moduleRepository.save(module));
    }

    public ModuleResponse getById(Long id) {
        return moduleMapper.toResponse(findEntityById(id));
    }

    public List<ModuleResponse> getAll() {
        return moduleRepository.findAll().stream().map(moduleMapper::toResponse).toList();
    }

    public List<ModuleResponse> getBySkill(Long skillId) {
        return moduleRepository.findBySkillId(skillId).stream()
                .map(moduleMapper::toResponse)
                .toList();
    }

    public ModuleResponse update(Long id, ModuleRequest request) {
        Module module = findEntityById(id);
        moduleMapper.updateEntity(module, request);
        module.setSkill(findSkillById(request.getSkillId()));
        return moduleMapper.toResponse(moduleRepository.save(module));
    }

    public void delete(Long id) {
        moduleRepository.delete(findEntityById(id));
    }

    private Module findEntityById(Long id) {
        return moduleRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found: " + id));
    }

    private Skill findSkillById(Long skillId) {
        return skillRepository
                .findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found: " + skillId));
    }
}
