package com.ahmadisyraf39.spabs_v2.team.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.ahmadisyraf39.spabs_v2.common.exception.ResourceNotFoundException;
import com.ahmadisyraf39.spabs_v2.team.dto.request.TeamRequest;
import com.ahmadisyraf39.spabs_v2.team.dto.response.TeamResponse;
import com.ahmadisyraf39.spabs_v2.team.entity.Team;
import com.ahmadisyraf39.spabs_v2.team.entity.enums.AgeGroup;
import com.ahmadisyraf39.spabs_v2.team.entity.enums.TeamCategory;
import com.ahmadisyraf39.spabs_v2.team.mapper.TeamMapper;
import com.ahmadisyraf39.spabs_v2.team.repository.TeamRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamMapper teamMapper;

    @InjectMocks
    private TeamService teamService;

    @Test
    void create_savesAndReturnsMappedResponse() {
        Team entity = new Team();
        when(teamMapper.toEntity(org.mockito.ArgumentMatchers.any())).thenReturn(entity);
        when(teamRepository.save(entity)).thenReturn(entity);
        when(teamMapper.toResponse(entity)).thenReturn(TeamResponse.builder().build());

        TeamRequest request = TeamRequest.builder()
                .name("Tigers U12 Boys")
                .category(TeamCategory.BOYS)
                .ageGroup(AgeGroup.U12)
                .build();

        assertThat(teamService.create(request)).isNotNull();
    }

    @Test
    void getById_notFound_throwsResourceNotFoundException() {
        when(teamRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.getById(1L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_notFound_throwsResourceNotFoundException() {
        when(teamRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.delete(1L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
