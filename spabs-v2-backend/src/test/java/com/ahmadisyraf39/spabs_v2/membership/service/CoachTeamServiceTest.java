package com.ahmadisyraf39.spabs_v2.membership.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.ahmadisyraf39.spabs_v2.common.exception.ResourceNotFoundException;
import com.ahmadisyraf39.spabs_v2.membership.dto.request.CoachTeamRequest;
import com.ahmadisyraf39.spabs_v2.membership.dto.response.CoachTeamResponse;
import com.ahmadisyraf39.spabs_v2.membership.entity.CoachTeam;
import com.ahmadisyraf39.spabs_v2.membership.entity.enums.CoachTeamRole;
import com.ahmadisyraf39.spabs_v2.membership.entity.enums.CoachTeamStatus;
import com.ahmadisyraf39.spabs_v2.membership.mapper.CoachTeamMapper;
import com.ahmadisyraf39.spabs_v2.membership.repository.CoachTeamRepository;
import com.ahmadisyraf39.spabs_v2.team.entity.Team;
import com.ahmadisyraf39.spabs_v2.team.repository.TeamRepository;
import com.ahmadisyraf39.spabs_v2.user.entity.Coach;
import com.ahmadisyraf39.spabs_v2.user.repository.CoachRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CoachTeamServiceTest {

    @Mock
    private CoachTeamRepository coachTeamRepository;

    @Mock
    private CoachRepository coachRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private CoachTeamMapper coachTeamMapper;

    @InjectMocks
    private CoachTeamService coachTeamService;

    @Test
    void create_resolvesCoachAndTeamAndSaves() {
        Coach coach = Coach.builder().id(20L).build();
        Team team = Team.builder().id(10L).build();
        when(coachRepository.findById(20L)).thenReturn(Optional.of(coach));
        when(teamRepository.findById(10L)).thenReturn(Optional.of(team));
        CoachTeam entity = new CoachTeam();
        when(coachTeamMapper.toEntity(org.mockito.ArgumentMatchers.any())).thenReturn(entity);
        when(coachTeamRepository.save(entity)).thenReturn(entity);
        when(coachTeamMapper.toResponse(entity)).thenReturn(CoachTeamResponse.builder().build());

        coachTeamService.create(CoachTeamRequest.builder()
                .coachId(20L)
                .teamId(10L)
                .role(CoachTeamRole.HEAD_COACH)
                .status(CoachTeamStatus.ACTIVE)
                .joinedAt(LocalDate.now())
                .build());

        assertThat(entity.getCoach()).isEqualTo(coach);
        assertThat(entity.getTeam()).isEqualTo(team);
    }

    @Test
    void create_coachNotFound_throwsResourceNotFoundException() {
        when(coachRepository.findById(20L)).thenReturn(Optional.empty());

        CoachTeamRequest request = CoachTeamRequest.builder()
                .coachId(20L)
                .teamId(10L)
                .role(CoachTeamRole.HEAD_COACH)
                .status(CoachTeamStatus.ACTIVE)
                .joinedAt(LocalDate.now())
                .build();

        assertThatThrownBy(() -> coachTeamService.create(request)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getById_notFound_throwsResourceNotFoundException() {
        when(coachTeamRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> coachTeamService.getById(1L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
