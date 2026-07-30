package com.ahmadisyraf39.spabs_v2.progress.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.ahmadisyraf39.spabs_v2.common.exception.ResourceNotFoundException;
import com.ahmadisyraf39.spabs_v2.progress.dto.request.ModuleRequest;
import com.ahmadisyraf39.spabs_v2.progress.dto.response.ModuleResponse;
import com.ahmadisyraf39.spabs_v2.progress.entity.Module;
import com.ahmadisyraf39.spabs_v2.progress.entity.Skill;
import com.ahmadisyraf39.spabs_v2.progress.mapper.ModuleMapper;
import com.ahmadisyraf39.spabs_v2.progress.repository.ModuleRepository;
import com.ahmadisyraf39.spabs_v2.progress.repository.SkillRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ModuleServiceTest {

    @Mock
    private ModuleRepository moduleRepository;

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private ModuleMapper moduleMapper;

    @InjectMocks
    private ModuleService moduleService;

    @Test
    void create_resolvesSkillAndSaves() {
        Skill skill = Skill.builder().id(1L).build();
        when(skillRepository.findById(1L)).thenReturn(Optional.of(skill));
        Module entity = new Module();
        when(moduleMapper.toEntity(org.mockito.ArgumentMatchers.any())).thenReturn(entity);
        when(moduleRepository.save(entity)).thenReturn(entity);
        when(moduleMapper.toResponse(entity)).thenReturn(ModuleResponse.builder().build());

        ModuleRequest request =
                ModuleRequest.builder().skillId(1L).name("Close control").build();

        moduleService.create(request);

        assertThat(entity.getSkill()).isEqualTo(skill);
    }

    @Test
    void create_skillNotFound_throwsResourceNotFoundException() {
        when(skillRepository.findById(1L)).thenReturn(Optional.empty());

        ModuleRequest request =
                ModuleRequest.builder().skillId(1L).name("Close control").build();

        assertThatThrownBy(() -> moduleService.create(request)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getById_notFound_throwsResourceNotFoundException() {
        when(moduleRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> moduleService.getById(1L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
