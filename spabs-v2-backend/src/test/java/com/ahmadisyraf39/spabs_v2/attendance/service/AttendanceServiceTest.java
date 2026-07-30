package com.ahmadisyraf39.spabs_v2.attendance.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ahmadisyraf39.spabs_v2.activity.entity.Activity;
import com.ahmadisyraf39.spabs_v2.activity.repository.ActivityRepository;
import com.ahmadisyraf39.spabs_v2.attendance.dto.request.AttendanceRequest;
import com.ahmadisyraf39.spabs_v2.attendance.dto.request.BulkAttendanceEntry;
import com.ahmadisyraf39.spabs_v2.attendance.dto.request.BulkAttendanceRequest;
import com.ahmadisyraf39.spabs_v2.attendance.dto.response.AttendanceResponse;
import com.ahmadisyraf39.spabs_v2.attendance.dto.response.AttendanceSummaryResponse;
import com.ahmadisyraf39.spabs_v2.attendance.entity.Attendance;
import com.ahmadisyraf39.spabs_v2.attendance.entity.enums.AttendanceStatus;
import com.ahmadisyraf39.spabs_v2.attendance.mapper.AttendanceMapper;
import com.ahmadisyraf39.spabs_v2.attendance.repository.AttendanceRepository;
import com.ahmadisyraf39.spabs_v2.common.exception.DuplicateResourceException;
import com.ahmadisyraf39.spabs_v2.membership.entity.CoachTeam;
import com.ahmadisyraf39.spabs_v2.membership.entity.enums.CoachTeamStatus;
import com.ahmadisyraf39.spabs_v2.membership.repository.CoachTeamRepository;
import com.ahmadisyraf39.spabs_v2.player.entity.Player;
import com.ahmadisyraf39.spabs_v2.player.repository.PlayerRepository;
import com.ahmadisyraf39.spabs_v2.security.UserPrincipal;
import com.ahmadisyraf39.spabs_v2.team.entity.Team;
import com.ahmadisyraf39.spabs_v2.team.repository.TeamRepository;
import com.ahmadisyraf39.spabs_v2.user.entity.Coach;
import com.ahmadisyraf39.spabs_v2.user.entity.User;
import com.ahmadisyraf39.spabs_v2.user.entity.enums.UserRole;
import com.ahmadisyraf39.spabs_v2.user.repository.CoachRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private AttendanceMapper attendanceMapper;

    @Mock
    private CoachRepository coachRepository;

    @Mock
    private CoachTeamRepository coachTeamRepository;

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    private Team team;
    private Activity activity;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(attendanceService, "editWindowDays", 3L);
        team = Team.builder().id(10L).name("Tigers").build();
        activity = Activity.builder()
                .id(100L)
                .team(team)
                .startAt(LocalDateTime.now().minusHours(2))
                .build();
    }

    private UserPrincipal principal(UserRole role, Long userId) {
        User user = User.builder()
                .id(userId)
                .email(role + "@spabs.example")
                .fullName("Test " + role)
                .role(role)
                .build();
        return new UserPrincipal(user);
    }

    @Test
    void create_asAdmin_isAlwaysAllowedRegardlessOfTiming() {
        UserPrincipal admin = principal(UserRole.ADMIN, 1L);
        AttendanceRequest request = AttendanceRequest.builder()
                .activityId(100L)
                .playerId(5L)
                .status(AttendanceStatus.PRESENT)
                .build();
        when(activityRepository.findById(100L)).thenReturn(Optional.of(activity));
        when(attendanceRepository.findByActivityIdAndPlayerId(100L, 5L)).thenReturn(Optional.empty());
        when(playerRepository.findById(5L)).thenReturn(Optional.of(Player.builder().id(5L).build()));
        Attendance entity = new Attendance();
        when(attendanceMapper.toEntity(request)).thenReturn(entity);
        when(attendanceRepository.save(entity)).thenReturn(entity);
        when(attendanceMapper.toResponse(entity)).thenReturn(AttendanceResponse.builder().id(1L).build());

        AttendanceResponse response = attendanceService.create(request, admin);

        assertThat(response).isNotNull();
        verify(attendanceRepository).save(entity);
    }

    @Test
    void create_asCoachWithActiveAssignmentWithinWindow_isAllowed() {
        UserPrincipal coachPrincipal = principal(UserRole.COACH, 2L);
        Coach coach = Coach.builder().id(20L).build();
        when(coachRepository.findByUserId(2L)).thenReturn(Optional.of(coach));
        CoachTeam assignment =
                CoachTeam.builder().team(team).status(CoachTeamStatus.ACTIVE).build();
        when(coachTeamRepository.findByCoachId(20L)).thenReturn(List.of(assignment));

        AttendanceRequest request = AttendanceRequest.builder()
                .activityId(100L)
                .playerId(5L)
                .status(AttendanceStatus.PRESENT)
                .build();
        when(activityRepository.findById(100L)).thenReturn(Optional.of(activity));
        when(attendanceRepository.findByActivityIdAndPlayerId(100L, 5L)).thenReturn(Optional.empty());
        when(playerRepository.findById(5L)).thenReturn(Optional.of(Player.builder().id(5L).build()));
        Attendance entity = new Attendance();
        when(attendanceMapper.toEntity(request)).thenReturn(entity);
        when(attendanceRepository.save(entity)).thenReturn(entity);
        when(attendanceMapper.toResponse(entity)).thenReturn(AttendanceResponse.builder().id(1L).build());

        AttendanceResponse response = attendanceService.create(request, coachPrincipal);

        assertThat(response).isNotNull();
    }

    @Test
    void create_asCoachNotAssignedToTeam_isDenied() {
        UserPrincipal coachPrincipal = principal(UserRole.COACH, 2L);
        Coach coach = Coach.builder().id(20L).build();
        when(coachRepository.findByUserId(2L)).thenReturn(Optional.of(coach));
        when(coachTeamRepository.findByCoachId(20L)).thenReturn(List.of());

        AttendanceRequest request = AttendanceRequest.builder()
                .activityId(100L)
                .playerId(5L)
                .status(AttendanceStatus.PRESENT)
                .build();
        when(activityRepository.findById(100L)).thenReturn(Optional.of(activity));

        assertThatThrownBy(() -> attendanceService.create(request, coachPrincipal))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Not assigned");
    }

    @Test
    void create_asCoachOutsideEditWindow_isDenied() {
        UserPrincipal coachPrincipal = principal(UserRole.COACH, 2L);
        Coach coach = Coach.builder().id(20L).build();
        when(coachRepository.findByUserId(2L)).thenReturn(Optional.of(coach));
        CoachTeam assignment =
                CoachTeam.builder().team(team).status(CoachTeamStatus.ACTIVE).build();
        when(coachTeamRepository.findByCoachId(20L)).thenReturn(List.of(assignment));

        Activity oldActivity = Activity.builder()
                .id(101L)
                .team(team)
                .startAt(LocalDateTime.now().minusDays(10))
                .build();
        AttendanceRequest request = AttendanceRequest.builder()
                .activityId(101L)
                .playerId(5L)
                .status(AttendanceStatus.PRESENT)
                .build();
        when(activityRepository.findById(101L)).thenReturn(Optional.of(oldActivity));

        assertThatThrownBy(() -> attendanceService.create(request, coachPrincipal))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Attendance can only be recorded");
    }

    @Test
    void create_asParent_isDenied() {
        UserPrincipal parentPrincipal = principal(UserRole.PARENT, 3L);
        AttendanceRequest request = AttendanceRequest.builder()
                .activityId(100L)
                .playerId(5L)
                .status(AttendanceStatus.PRESENT)
                .build();
        when(activityRepository.findById(100L)).thenReturn(Optional.of(activity));

        assertThatThrownBy(() -> attendanceService.create(request, parentPrincipal))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void create_duplicateRecord_throwsDuplicateResourceException() {
        UserPrincipal admin = principal(UserRole.ADMIN, 1L);
        AttendanceRequest request = AttendanceRequest.builder()
                .activityId(100L)
                .playerId(5L)
                .status(AttendanceStatus.PRESENT)
                .build();
        when(activityRepository.findById(100L)).thenReturn(Optional.of(activity));
        when(attendanceRepository.findByActivityIdAndPlayerId(100L, 5L))
                .thenReturn(Optional.of(Attendance.builder().id(999L).build()));

        assertThatThrownBy(() -> attendanceService.create(request, admin))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void saveBulk_upsertsEachEntryAgainstTheActivity() {
        UserPrincipal admin = principal(UserRole.ADMIN, 1L);
        when(activityRepository.findById(100L)).thenReturn(Optional.of(activity));
        when(attendanceRepository.findByActivityIdAndPlayerId(100L, 5L)).thenReturn(Optional.empty());
        when(attendanceRepository.findByActivityIdAndPlayerId(100L, 6L))
                .thenReturn(Optional.of(Attendance.builder().id(50L).build()));
        when(playerRepository.findById(5L)).thenReturn(Optional.of(Player.builder().id(5L).build()));
        when(attendanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(attendanceMapper.toResponse(any())).thenReturn(AttendanceResponse.builder().build());

        BulkAttendanceRequest request = BulkAttendanceRequest.builder()
                .activityId(100L)
                .entries(List.of(
                        BulkAttendanceEntry.builder()
                                .playerId(5L)
                                .status(AttendanceStatus.PRESENT)
                                .build(),
                        BulkAttendanceEntry.builder()
                                .playerId(6L)
                                .status(AttendanceStatus.LATE)
                                .build()))
                .build();

        List<AttendanceResponse> responses = attendanceService.saveBulk(request, admin);

        assertThat(responses).hasSize(2);
        verify(attendanceRepository, times(2)).save(any());
    }

    @Test
    void getSummary_calculatesPercentageExcludingExcusedFromDenominator() {
        when(playerRepository.findById(5L)).thenReturn(Optional.of(Player.builder().id(5L).build()));
        when(teamRepository.existsById(10L)).thenReturn(true);

        List<Attendance> records = List.of(
                Attendance.builder().status(AttendanceStatus.PRESENT).activity(activity).build(),
                Attendance.builder().status(AttendanceStatus.PRESENT).activity(activity).build(),
                Attendance.builder().status(AttendanceStatus.LATE).activity(activity).build(),
                Attendance.builder().status(AttendanceStatus.ABSENT).activity(activity).build(),
                Attendance.builder().status(AttendanceStatus.EXCUSED).activity(activity).build());
        when(attendanceRepository.findByPlayerId(5L)).thenReturn(records);
        when(attendanceMapper.toResponse(any())).thenReturn(AttendanceResponse.builder().build());

        AttendanceSummaryResponse summary = attendanceService.getSummary(5L, 10L);

        // total=5, excused=1, considered=4, present+late=3 -> 75.0%
        assertThat(summary.getTotalRecords()).isEqualTo(5);
        assertThat(summary.getPresentCount()).isEqualTo(2);
        assertThat(summary.getLateCount()).isEqualTo(1);
        assertThat(summary.getAbsentCount()).isEqualTo(1);
        assertThat(summary.getExcusedCount()).isEqualTo(1);
        assertThat(summary.getAttendancePercentage()).isEqualTo(75.0);
    }

    @Test
    void getSummary_noRecords_returnsZeroPercentage() {
        when(playerRepository.findById(5L)).thenReturn(Optional.of(Player.builder().id(5L).build()));
        when(teamRepository.existsById(10L)).thenReturn(true);
        when(attendanceRepository.findByPlayerId(5L)).thenReturn(List.of());

        AttendanceSummaryResponse summary = attendanceService.getSummary(5L, 10L);

        assertThat(summary.getAttendancePercentage()).isEqualTo(0.0);
    }

    @Test
    void getSummary_allExcused_doesNotDivideByZero() {
        when(playerRepository.findById(5L)).thenReturn(Optional.of(Player.builder().id(5L).build()));
        when(teamRepository.existsById(10L)).thenReturn(true);
        when(attendanceRepository.findByPlayerId(5L))
                .thenReturn(List.of(Attendance.builder()
                        .status(AttendanceStatus.EXCUSED)
                        .activity(activity)
                        .build()));
        when(attendanceMapper.toResponse(any())).thenReturn(AttendanceResponse.builder().build());

        AttendanceSummaryResponse summary = attendanceService.getSummary(5L, 10L);

        assertThat(summary.getAttendancePercentage()).isEqualTo(0.0);
    }
}
