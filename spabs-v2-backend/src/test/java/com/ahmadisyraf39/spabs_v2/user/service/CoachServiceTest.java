package com.ahmadisyraf39.spabs_v2.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.ahmadisyraf39.spabs_v2.common.exception.ResourceNotFoundException;
import com.ahmadisyraf39.spabs_v2.security.UserPrincipal;
import com.ahmadisyraf39.spabs_v2.user.dto.request.CoachRequest;
import com.ahmadisyraf39.spabs_v2.user.dto.response.CoachResponse;
import com.ahmadisyraf39.spabs_v2.user.entity.Coach;
import com.ahmadisyraf39.spabs_v2.user.entity.User;
import com.ahmadisyraf39.spabs_v2.user.entity.enums.CoachCertification;
import com.ahmadisyraf39.spabs_v2.user.entity.enums.CoachSpecialization;
import com.ahmadisyraf39.spabs_v2.user.mapper.CoachMapper;
import com.ahmadisyraf39.spabs_v2.user.repository.CoachRepository;
import com.ahmadisyraf39.spabs_v2.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CoachServiceTest {

    @Mock
    private CoachRepository coachRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CoachMapper coachMapper;

    @InjectMocks
    private CoachService coachService;

    @Test
    void create_resolvesUserAndSaves() {
        User user = User.builder().id(1L).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Coach entity = new Coach();
        when(coachMapper.toEntity(org.mockito.ArgumentMatchers.any())).thenReturn(entity);
        when(coachRepository.save(entity)).thenReturn(entity);
        when(coachMapper.toResponse(entity)).thenReturn(CoachResponse.builder().build());

        coachService.create(CoachRequest.builder()
                .userId(1L)
                .specialization(CoachSpecialization.GENERAL)
                .certification(CoachCertification.FAM_B)
                .build());

        assertThat(entity.getUser()).isEqualTo(user);
    }

    @Test
    void create_userNotFound_throwsResourceNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        CoachRequest request = CoachRequest.builder()
                .userId(1L)
                .specialization(CoachSpecialization.GENERAL)
                .certification(CoachCertification.FAM_B)
                .build();

        assertThatThrownBy(() -> coachService.create(request)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getById_notFound_throwsResourceNotFoundException() {
        when(coachRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> coachService.getById(1L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getMyProfile_found_returnsResponse() {
        User user = User.builder().id(7L).build();
        Coach coach = new Coach();
        when(coachRepository.findByUserId(7L)).thenReturn(Optional.of(coach));
        when(coachMapper.toResponse(coach)).thenReturn(CoachResponse.builder().id(3L).build());

        CoachResponse response = coachService.getMyProfile(new UserPrincipal(user));

        assertThat(response.getId()).isEqualTo(3L);
    }

    @Test
    void getMyProfile_noCoachProfile_throwsResourceNotFoundException() {
        User user = User.builder().id(7L).build();
        when(coachRepository.findByUserId(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> coachService.getMyProfile(new UserPrincipal(user)))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
