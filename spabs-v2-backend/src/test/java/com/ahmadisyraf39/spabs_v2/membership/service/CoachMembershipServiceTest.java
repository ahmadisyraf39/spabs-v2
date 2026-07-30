package com.ahmadisyraf39.spabs_v2.membership.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.ahmadisyraf39.spabs_v2.common.exception.ResourceNotFoundException;
import com.ahmadisyraf39.spabs_v2.membership.dto.request.CoachMembershipRequest;
import com.ahmadisyraf39.spabs_v2.membership.dto.response.CoachMembershipResponse;
import com.ahmadisyraf39.spabs_v2.membership.entity.CoachMembership;
import com.ahmadisyraf39.spabs_v2.membership.entity.enums.CoachMembershipStatus;
import com.ahmadisyraf39.spabs_v2.membership.mapper.CoachMembershipMapper;
import com.ahmadisyraf39.spabs_v2.membership.repository.CoachMembershipRepository;
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
class CoachMembershipServiceTest {

    @Mock
    private CoachMembershipRepository coachMembershipRepository;

    @Mock
    private CoachRepository coachRepository;

    @Mock
    private CoachMembershipMapper coachMembershipMapper;

    @InjectMocks
    private CoachMembershipService coachMembershipService;

    @Test
    void create_resolvesCoachAndSaves() {
        Coach coach = Coach.builder().id(20L).build();
        when(coachRepository.findById(20L)).thenReturn(Optional.of(coach));
        CoachMembership entity = new CoachMembership();
        when(coachMembershipMapper.toEntity(org.mockito.ArgumentMatchers.any())).thenReturn(entity);
        when(coachMembershipRepository.save(entity)).thenReturn(entity);
        when(coachMembershipMapper.toResponse(entity)).thenReturn(CoachMembershipResponse.builder().build());

        coachMembershipService.create(CoachMembershipRequest.builder()
                .coachId(20L)
                .status(CoachMembershipStatus.ACTIVE)
                .joinedAt(LocalDate.now())
                .build());

        assertThat(entity.getCoach()).isEqualTo(coach);
    }

    @Test
    void create_coachNotFound_throwsResourceNotFoundException() {
        when(coachRepository.findById(20L)).thenReturn(Optional.empty());

        CoachMembershipRequest request = CoachMembershipRequest.builder()
                .coachId(20L)
                .status(CoachMembershipStatus.ACTIVE)
                .joinedAt(LocalDate.now())
                .build();

        assertThatThrownBy(() -> coachMembershipService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getById_notFound_throwsResourceNotFoundException() {
        when(coachMembershipRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> coachMembershipService.getById(1L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
