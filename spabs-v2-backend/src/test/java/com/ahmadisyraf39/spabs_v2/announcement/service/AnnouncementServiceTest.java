package com.ahmadisyraf39.spabs_v2.announcement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ahmadisyraf39.spabs_v2.announcement.dto.request.AnnouncementRequest;
import com.ahmadisyraf39.spabs_v2.announcement.dto.response.AnnouncementResponse;
import com.ahmadisyraf39.spabs_v2.announcement.entity.Announcement;
import com.ahmadisyraf39.spabs_v2.announcement.mapper.AnnouncementMapper;
import com.ahmadisyraf39.spabs_v2.announcement.repository.AnnouncementRepository;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class AnnouncementServiceTest {

    @Mock
    private AnnouncementRepository announcementRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private AnnouncementMapper announcementMapper;

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
    private AnnouncementService announcementService;

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
    void create_withTeamId_resolvesTeamAndAttributesCreator() {
        UserPrincipal admin = principal(UserRole.ADMIN, 1L);
        Team team = Team.builder().id(10L).build();
        when(teamRepository.findById(10L)).thenReturn(Optional.of(team));
        Announcement entity = new Announcement();
        when(announcementMapper.toEntity(any())).thenReturn(entity);
        when(announcementRepository.save(entity)).thenReturn(entity);
        when(announcementMapper.toResponse(entity)).thenReturn(AnnouncementResponse.builder().build());

        AnnouncementRequest request = AnnouncementRequest.builder()
                .teamId(10L)
                .title("Training moved")
                .content("Details")
                .build();

        announcementService.create(request, admin);

        assertThat(entity.getTeam()).isEqualTo(team);
        assertThat(entity.getCreatedBy()).isEqualTo(admin.getUser());
    }

    @Test
    void create_withoutTeamId_isGlobal() {
        UserPrincipal admin = principal(UserRole.ADMIN, 1L);
        Announcement entity = new Announcement();
        when(announcementMapper.toEntity(any())).thenReturn(entity);
        when(announcementRepository.save(entity)).thenReturn(entity);
        when(announcementMapper.toResponse(entity)).thenReturn(AnnouncementResponse.builder().build());

        AnnouncementRequest request =
                AnnouncementRequest.builder().title("Holiday").content("Details").build();

        announcementService.create(request, admin);

        assertThat(entity.getTeam()).isNull();
        verify(teamRepository, never()).findById(any());
    }

    @Test
    void getMine_asAdmin_delegatesToGetAll() {
        when(announcementRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of());

        List<AnnouncementResponse> result = announcementService.getMine(principal(UserRole.ADMIN, 1L));

        assertThat(result).isEmpty();
        verify(announcementRepository).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void getMine_asCoach_returnsGlobalPlusOwnActiveTeamOnly() {
        Coach coach = Coach.builder().id(20L).build();
        when(coachRepository.findByUserId(2L)).thenReturn(Optional.of(coach));
        Team activeTeam = Team.builder().id(10L).build();
        Team inactiveTeam = Team.builder().id(11L).build();
        when(coachTeamRepository.findByCoachId(20L))
                .thenReturn(List.of(
                        CoachTeam.builder().team(activeTeam).status(CoachTeamStatus.ACTIVE).build(),
                        CoachTeam.builder().team(inactiveTeam).status(CoachTeamStatus.INACTIVE).build()));

        Announcement global = new Announcement();
        global.setCreatedAt(LocalDateTime.now().minusDays(1));
        Announcement teamScoped = new Announcement();
        teamScoped.setCreatedAt(LocalDateTime.now());
        when(announcementRepository.findByTeamIdIsNull()).thenReturn(List.of(global));
        when(announcementRepository.findByTeamIdIn(List.of(10L))).thenReturn(List.of(teamScoped));
        when(announcementMapper.toResponse(any())).thenReturn(AnnouncementResponse.builder().build());

        List<AnnouncementResponse> result = announcementService.getMine(principal(UserRole.COACH, 2L));

        assertThat(result).hasSize(2);
        verify(announcementRepository, never()).findByTeamIdIn(List.of(11L));
    }

    @Test
    void getMine_asCoachWithNoActiveTeams_returnsOnlyGlobalWithoutQueryingTeamScoped() {
        Coach coach = Coach.builder().id(20L).build();
        when(coachRepository.findByUserId(2L)).thenReturn(Optional.of(coach));
        when(coachTeamRepository.findByCoachId(20L)).thenReturn(List.of());
        when(announcementRepository.findByTeamIdIsNull()).thenReturn(List.of(new Announcement()));
        when(announcementMapper.toResponse(any())).thenReturn(AnnouncementResponse.builder().build());

        List<AnnouncementResponse> result = announcementService.getMine(principal(UserRole.COACH, 2L));

        assertThat(result).hasSize(1);
        verify(announcementRepository, never()).findByTeamIdIn(any());
    }

    @Test
    void getMine_asParent_returnsGlobalPlusChildrensActiveTeamsOnly() {
        Parent parent = Parent.builder().id(30L).build();
        when(parentRepository.findByUserId(3L)).thenReturn(Optional.of(parent));
        Player child = Player.builder().id(5L).build();
        when(playerParentRepository.findByParentId(30L))
                .thenReturn(List.of(PlayerParent.builder().player(child).build()));
        Team activeTeam = Team.builder().id(10L).build();
        Team leftTeam = Team.builder().id(11L).build();
        when(playerTeamRepository.findByPlayerId(5L))
                .thenReturn(List.of(
                        PlayerTeam.builder().team(activeTeam).status(PlayerTeamStatus.ACTIVE).build(),
                        PlayerTeam.builder().team(leftTeam).status(PlayerTeamStatus.INACTIVE).build()));
        when(announcementRepository.findByTeamIdIsNull()).thenReturn(List.of());
        when(announcementRepository.findByTeamIdIn(List.of(10L))).thenReturn(List.of(new Announcement()));
        when(announcementMapper.toResponse(any())).thenReturn(AnnouncementResponse.builder().build());

        List<AnnouncementResponse> result = announcementService.getMine(principal(UserRole.PARENT, 3L));

        assertThat(result).hasSize(1);
    }

    @Test
    void getMine_asCoachWithNoCoachProfile_isDenied() {
        when(coachRepository.findByUserId(4L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> announcementService.getMine(principal(UserRole.COACH, 4L)))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getMine_sortsResultsByCreatedAtDescending() {
        Coach coach = Coach.builder().id(20L).build();
        when(coachRepository.findByUserId(2L)).thenReturn(Optional.of(coach));
        when(coachTeamRepository.findByCoachId(20L)).thenReturn(List.of());

        Announcement older = new Announcement();
        older.setTitle("older");
        older.setCreatedAt(LocalDateTime.now().minusDays(5));
        Announcement newer = new Announcement();
        newer.setTitle("newer");
        newer.setCreatedAt(LocalDateTime.now());
        when(announcementRepository.findByTeamIdIsNull()).thenReturn(List.of(older, newer));
        when(announcementMapper.toResponse(older))
                .thenReturn(AnnouncementResponse.builder().title("older").build());
        when(announcementMapper.toResponse(newer))
                .thenReturn(AnnouncementResponse.builder().title("newer").build());

        List<AnnouncementResponse> result = announcementService.getMine(principal(UserRole.COACH, 2L));

        assertThat(result).extracting(AnnouncementResponse::getTitle).containsExactly("newer", "older");
    }
}
