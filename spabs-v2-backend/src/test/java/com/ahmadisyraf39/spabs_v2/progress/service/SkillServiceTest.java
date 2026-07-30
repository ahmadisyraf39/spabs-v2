package com.ahmadisyraf39.spabs_v2.progress.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.ahmadisyraf39.spabs_v2.common.exception.ResourceNotFoundException;
import com.ahmadisyraf39.spabs_v2.progress.dto.request.SkillRequest;
import com.ahmadisyraf39.spabs_v2.progress.dto.response.SkillResponse;
import com.ahmadisyraf39.spabs_v2.progress.entity.Skill;
import com.ahmadisyraf39.spabs_v2.progress.mapper.SkillMapper;
import com.ahmadisyraf39.spabs_v2.progress.repository.SkillRepository;
import com.ahmadisyraf39.spabs_v2.team.entity.enums.AgeGroup;
import com.ahmadisyraf39.spabs_v2.team.entity.enums.TeamCategory;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SkillServiceTest {

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private SkillMapper skillMapper;

    @InjectMocks
    private SkillService skillService;

    @Test
    void create_savesAndReturnsMappedResponse() {
        Skill entity = new Skill();
        when(skillMapper.toEntity(org.mockito.ArgumentMatchers.any())).thenReturn(entity);
        when(skillRepository.save(entity)).thenReturn(entity);
        when(skillMapper.toResponse(entity)).thenReturn(SkillResponse.builder().build());

        SkillRequest request = SkillRequest.builder()
                .ageGroup(AgeGroup.U12)
                .category(TeamCategory.BOYS)
                .name("Dribbling")
                .build();

        assertThat(skillService.create(request)).isNotNull();
    }

    @Test
    void getById_notFound_throwsResourceNotFoundException() {
        when(skillRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> skillService.getById(1L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getByAgeGroupAndCategory_delegatesToRepository() {
        when(skillRepository.findByAgeGroupAndCategory(AgeGroup.U12, TeamCategory.BOYS))
                .thenReturn(List.of(new Skill()));
        when(skillMapper.toResponse(org.mockito.ArgumentMatchers.any()))
                .thenReturn(SkillResponse.builder().build());

        assertThat(skillService.getByAgeGroupAndCategory(AgeGroup.U12, TeamCategory.BOYS)).hasSize(1);
    }
}
