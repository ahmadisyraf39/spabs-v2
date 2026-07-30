package com.ahmadisyraf39.spabs_v2.activity.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ahmadisyraf39.spabs_v2.activity.dto.request.RecurringActivityRequest;
import com.ahmadisyraf39.spabs_v2.activity.dto.response.ActivityResponse;
import com.ahmadisyraf39.spabs_v2.activity.entity.Activity;
import com.ahmadisyraf39.spabs_v2.activity.entity.enums.ActivityType;
import com.ahmadisyraf39.spabs_v2.activity.mapper.ActivityMapper;
import com.ahmadisyraf39.spabs_v2.activity.repository.ActivityRepository;
import com.ahmadisyraf39.spabs_v2.common.exception.InvalidRequestException;
import com.ahmadisyraf39.spabs_v2.membership.entity.CoachTeam;
import com.ahmadisyraf39.spabs_v2.membership.entity.PlayerParent;
import com.ahmadisyraf39.spabs_v2.membership.entity.PlayerTeam;
import com.ahmadisyraf39.spabs_v2.membership.entity.enums.CoachTeamStatus;
import com.ahmadisyraf39.spabs_v2.membership.entity.enums.PlayerTeamStatus;
import com.ahmadisyraf39.spabs_v2.membership.repository.CoachTeamRepository;
import com.ahmadisyraf39.spabs_v2.membership.repository.PlayerParentRepository;
import com.ahmadisyraf39.spabs_v2.membership.repository.PlayerTeamRepository;
import com.ahmadisyraf39.spabs_v2.player.entity.Player;
import com.ahmadisyraf39.spabs_v2.security.UserPrincipal;
import com.ahmadisyraf39.spabs_v2.team.entity.Team;
import com.ahmadisyraf39.spabs_v2.team.repository.TeamRepository;
import com.ahmadisyraf39.spabs_v2.user.entity.Coach;
import com.ahmadisyraf39.spabs_v2.user.entity.Parent;
import com.ahmadisyraf39.spabs_v2.user.entity.User;
import com.ahmadisyraf39.spabs_v2.user.entity.enums.UserRole;
import com.ahmadisyraf39.spabs_v2.user.repository.CoachRepository;
import com.ahmadisyraf39.spabs_v2.user.repository.ParentRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private ActivityMapper activityMapper;

    @Mock
    private CoachRepository coachRepository;

    @Mock
    private ParentRepository parentRepository;

    @Mock
    private CoachTeamRepository coachTeamRepository;

    @Mock
    private PlayerParentRepository playerParentRepository;

    @Mock
    private PlayerTeamRepository playerTeamRepository;

    @InjectMocks
    private ActivityService activityService;

    private UserPrincipal principal(UserRole role, Long userId) {
        User user = User.builder()
                .id(userId)
                .email(role + "@spabs.example")
                .fullName("Test")
                .role(role)
                .build();
        return new UserPrincipal(user);
    }

    @Test
    void createRecurring_generatesOneActivityPerMatchingDayOfWeekPerTeam() {
        Team team = Team.builder().id(10L).build();
        when(teamRepository.findById(10L)).thenReturn(Optional.of(team));
        when(activityRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
        when(activityMapper.toResponse(any())).thenReturn(ActivityResponse.builder().build());

        // 2025-01-06, 13, 20 are the three Mondays in this range.
        RecurringActivityRequest request = RecurringActivityRequest.builder()
                .teamIds(List.of(10L))
                .type(ActivityType.TRAINING)
                .title("Weekly Training")
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(17, 0))
                .startDate(LocalDate.of(2025, 1, 6))
                .endDate(LocalDate.of(2025, 1, 20))
                .build();

        List<ActivityResponse> responses = activityService.createRecurring(request);

        assertThat(responses).hasSize(3);
        ArgumentCaptor<List<Activity>> captor = ArgumentCaptor.forClass(List.class);
        verify(activityRepository).saveAll(captor.capture());
        assertThat(captor.getValue())
                .extracting(a -> a.getStartAt().toLocalDate())
                .containsExactlyInAnyOrder(
                        LocalDate.of(2025, 1, 6), LocalDate.of(2025, 1, 13), LocalDate.of(2025, 1, 20));
    }

    @Test
    void createRecurring_noDatesMatchDayOfWeek_throwsInvalidRequestException() {
        when(teamRepository.findById(10L)).thenReturn(Optional.of(Team.builder().id(10L).build()));

        RecurringActivityRequest request = RecurringActivityRequest.builder()
                .teamIds(List.of(10L))
                .type(ActivityType.TRAINING)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(17, 0))
                .startDate(LocalDate.of(2025, 1, 7)) // Tuesday
                .endDate(LocalDate.of(2025, 1, 7)) // same day, not a Monday
                .build();

        assertThatThrownBy(() -> activityService.createRecurring(request)).isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void createRecurring_exceedsSafetyCap_throwsInvalidRequestException() {
        Team teamA = Team.builder().id(10L).build();
        Team teamB = Team.builder().id(11L).build();
        when(teamRepository.findById(10L)).thenReturn(Optional.of(teamA));
        when(teamRepository.findById(11L)).thenReturn(Optional.of(teamB));

        // ~10 years of weekly dates x 2 teams comfortably exceeds the 500-activity cap.
        RecurringActivityRequest request = RecurringActivityRequest.builder()
                .teamIds(List.of(10L, 11L))
                .type(ActivityType.TRAINING)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(17, 0))
                .startDate(LocalDate.of(2020, 1, 6))
                .endDate(LocalDate.of(2030, 1, 6))
                .build();

        assertThatThrownBy(() -> activityService.createRecurring(request)).isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void createRecurring_applyToAllTeams_ignoresTeamIdsAndUsesEveryTeam() {
        Team teamA = Team.builder().id(10L).build();
        Team teamB = Team.builder().id(11L).build();
        when(teamRepository.findAll()).thenReturn(List.of(teamA, teamB));
        when(activityRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
        when(activityMapper.toResponse(any())).thenReturn(ActivityResponse.builder().build());

        RecurringActivityRequest request = RecurringActivityRequest.builder()
                .teamIds(List.of(999L)) // should be ignored
                .applyToAllTeams(true)
                .type(ActivityType.TRAINING)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(17, 0))
                .startDate(LocalDate.of(2025, 1, 6))
                .endDate(LocalDate.of(2025, 1, 6))
                .build();

        List<ActivityResponse> responses = activityService.createRecurring(request);

        assertThat(responses).hasSize(2); // one per team, teamRepository.findById never consulted
        verify(teamRepository, never()).findById(any());
    }

    @Test
    void createRecurring_noTeamIdsAndNotApplyToAll_throwsInvalidRequestException() {
        RecurringActivityRequest request = RecurringActivityRequest.builder()
                .type(ActivityType.TRAINING)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(17, 0))
                .startDate(LocalDate.of(2025, 1, 6))
                .endDate(LocalDate.of(2025, 1, 6))
                .build();

        assertThatThrownBy(() -> activityService.createRecurring(request)).isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void getUpcomingForCoach_asAdmin_canViewAnyCoach() {
        when(coachTeamRepository.findByCoachId(20L)).thenReturn(List.of());

        List<ActivityResponse> result = activityService.getUpcomingForCoach(20L, principal(UserRole.ADMIN, 1L));

        assertThat(result).isEmpty();
    }

    @Test
    void getUpcomingForCoach_asOtherCoach_isDenied() {
        Coach ownCoach = Coach.builder().id(21L).build();
        when(coachRepository.findByUserId(2L)).thenReturn(Optional.of(ownCoach));

        assertThatThrownBy(() -> activityService.getUpcomingForCoach(20L, principal(UserRole.COACH, 2L)))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getUpcomingForCoach_filtersInactiveAssignmentsAndDedupesTeams() {
        Coach ownCoach = Coach.builder().id(20L).build();
        when(coachRepository.findByUserId(2L)).thenReturn(Optional.of(ownCoach));

        Team teamA = Team.builder().id(10L).build();
        Team teamInactive = Team.builder().id(99L).build();
        when(coachTeamRepository.findByCoachId(20L))
                .thenReturn(List.of(
                        CoachTeam.builder().team(teamA).status(CoachTeamStatus.ACTIVE).build(),
                        CoachTeam.builder().team(teamA).status(CoachTeamStatus.ACTIVE).build(), // duplicate team
                        CoachTeam.builder().team(teamInactive).status(CoachTeamStatus.INACTIVE).build()));
        when(activityRepository.findByTeamIdInAndStartAtAfter(anyList(), any())).thenReturn(List.of());

        activityService.getUpcomingForCoach(20L, principal(UserRole.COACH, 2L));

        ArgumentCaptor<List<Long>> captor = ArgumentCaptor.forClass(List.class);
        verify(activityRepository).findByTeamIdInAndStartAtAfter(captor.capture(), any());
        assertThat(captor.getValue()).containsExactly(10L);
    }

    @Test
    void getUpcomingForParent_dedupesTeamsAcrossMultipleChildrenAndFiltersActiveOnly() {
        Parent parent = Parent.builder().id(30L).build();
        when(parentRepository.findByUserId(3L)).thenReturn(Optional.of(parent));

        Player child1 = Player.builder().id(5L).build();
        Player child2 = Player.builder().id(6L).build();
        when(playerParentRepository.findByParentId(30L))
                .thenReturn(List.of(
                        PlayerParent.builder().player(child1).build(),
                        PlayerParent.builder().player(child2).build()));

        Team sharedTeam = Team.builder().id(10L).build();
        Team leftTeam = Team.builder().id(20L).build();
        when(playerTeamRepository.findByPlayerId(5L))
                .thenReturn(List.of(PlayerTeam.builder()
                        .team(sharedTeam)
                        .status(PlayerTeamStatus.ACTIVE)
                        .build()));
        when(playerTeamRepository.findByPlayerId(6L))
                .thenReturn(List.of(
                        PlayerTeam.builder().team(sharedTeam).status(PlayerTeamStatus.ACTIVE).build(),
                        PlayerTeam.builder().team(leftTeam).status(PlayerTeamStatus.INACTIVE).build()));
        when(activityRepository.findByTeamIdInAndStartAtAfter(anyList(), any())).thenReturn(List.of());

        activityService.getUpcomingForParent(30L, principal(UserRole.PARENT, 3L));

        ArgumentCaptor<List<Long>> captor = ArgumentCaptor.forClass(List.class);
        verify(activityRepository).findByTeamIdInAndStartAtAfter(captor.capture(), any());
        assertThat(captor.getValue()).containsExactly(10L);
    }

    @Test
    void getMyUpcoming_asCoach_delegatesToOwnCoachId() {
        Coach coach = Coach.builder().id(20L).build();
        when(coachRepository.findByUserId(2L)).thenReturn(Optional.of(coach));
        when(coachTeamRepository.findByCoachId(20L)).thenReturn(List.of());

        List<ActivityResponse> result = activityService.getMyUpcoming(principal(UserRole.COACH, 2L));

        assertThat(result).isEmpty();
    }

    @Test
    void getMyUpcoming_asParent_delegatesToOwnParentId() {
        Parent parent = Parent.builder().id(30L).build();
        when(parentRepository.findByUserId(3L)).thenReturn(Optional.of(parent));
        when(playerParentRepository.findByParentId(30L)).thenReturn(List.of());

        List<ActivityResponse> result = activityService.getMyUpcoming(principal(UserRole.PARENT, 3L));

        assertThat(result).isEmpty();
    }

    @Test
    void getMyUpcoming_asAdmin_isDenied_noPersonalUpcomingView() {
        assertThatThrownBy(() -> activityService.getMyUpcoming(principal(UserRole.ADMIN, 1L)))
                .isInstanceOf(AccessDeniedException.class);
    }
}
