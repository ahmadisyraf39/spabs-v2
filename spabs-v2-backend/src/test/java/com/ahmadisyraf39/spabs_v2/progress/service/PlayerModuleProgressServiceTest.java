package com.ahmadisyraf39.spabs_v2.progress.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.ahmadisyraf39.spabs_v2.common.exception.InvalidRequestException;
import com.ahmadisyraf39.spabs_v2.membership.entity.CoachTeam;
import com.ahmadisyraf39.spabs_v2.membership.entity.PlayerParent;
import com.ahmadisyraf39.spabs_v2.membership.entity.enums.CoachTeamStatus;
import com.ahmadisyraf39.spabs_v2.membership.repository.CoachTeamRepository;
import com.ahmadisyraf39.spabs_v2.membership.repository.PlayerParentRepository;
import com.ahmadisyraf39.spabs_v2.player.entity.Player;
import com.ahmadisyraf39.spabs_v2.player.repository.PlayerRepository;
import com.ahmadisyraf39.spabs_v2.progress.dto.request.BulkPlayerModuleProgressEntry;
import com.ahmadisyraf39.spabs_v2.progress.dto.request.BulkPlayerModuleProgressRequest;
import com.ahmadisyraf39.spabs_v2.progress.dto.request.PlayerModuleProgressRequest;
import com.ahmadisyraf39.spabs_v2.progress.dto.response.PlayerModuleProgressResponse;
import com.ahmadisyraf39.spabs_v2.progress.dto.response.PlayerTeamProgressResponse;
import com.ahmadisyraf39.spabs_v2.progress.entity.Module;
import com.ahmadisyraf39.spabs_v2.progress.entity.PlayerModuleProgress;
import com.ahmadisyraf39.spabs_v2.progress.entity.Skill;
import com.ahmadisyraf39.spabs_v2.progress.entity.enums.ModuleProgressStatus;
import com.ahmadisyraf39.spabs_v2.progress.mapper.PlayerModuleProgressMapper;
import com.ahmadisyraf39.spabs_v2.progress.repository.ModuleRepository;
import com.ahmadisyraf39.spabs_v2.progress.repository.PlayerModuleProgressRepository;
import com.ahmadisyraf39.spabs_v2.progress.repository.SkillRepository;
import com.ahmadisyraf39.spabs_v2.security.UserPrincipal;
import com.ahmadisyraf39.spabs_v2.team.entity.Team;
import com.ahmadisyraf39.spabs_v2.team.entity.enums.AgeGroup;
import com.ahmadisyraf39.spabs_v2.team.entity.enums.TeamCategory;
import com.ahmadisyraf39.spabs_v2.team.repository.TeamRepository;
import com.ahmadisyraf39.spabs_v2.user.entity.Coach;
import com.ahmadisyraf39.spabs_v2.user.entity.Parent;
import com.ahmadisyraf39.spabs_v2.user.entity.User;
import com.ahmadisyraf39.spabs_v2.user.entity.enums.UserRole;
import com.ahmadisyraf39.spabs_v2.user.repository.CoachRepository;
import com.ahmadisyraf39.spabs_v2.user.repository.ParentRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class PlayerModuleProgressServiceTest {

    @Mock
    private PlayerModuleProgressRepository playerModuleProgressRepository;

    @Mock
    private ModuleRepository moduleRepository;

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private CoachRepository coachRepository;

    @Mock
    private CoachTeamRepository coachTeamRepository;

    @Mock
    private ParentRepository parentRepository;

    @Mock
    private PlayerParentRepository playerParentRepository;

    @Mock
    private PlayerModuleProgressMapper playerModuleProgressMapper;

    @InjectMocks
    private PlayerModuleProgressService playerModuleProgressService;

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
    void save_teamAgeGroupMismatchesSkill_throwsInvalidRequestException() {
        Team team = Team.builder().id(10L).ageGroup(AgeGroup.U12).category(TeamCategory.BOYS).build();
        Skill skill = Skill.builder().id(1L).ageGroup(AgeGroup.U14).category(TeamCategory.BOYS).build();
        Module module = Module.builder().id(1L).skill(skill).build();
        when(moduleRepository.findById(1L)).thenReturn(Optional.of(module));
        when(teamRepository.findById(10L)).thenReturn(Optional.of(team));

        PlayerModuleProgressRequest request = PlayerModuleProgressRequest.builder()
                .moduleId(1L)
                .playerId(5L)
                .teamId(10L)
                .status(ModuleProgressStatus.STARTED)
                .build();

        assertThatThrownBy(() -> playerModuleProgressService.save(request, principal(UserRole.ADMIN, 1L)))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void save_asAdmin_recordsProgressWithNoCoachAttribution() {
        Team team = Team.builder().id(10L).ageGroup(AgeGroup.U12).category(TeamCategory.BOYS).build();
        Skill skill = Skill.builder().id(1L).ageGroup(AgeGroup.U12).category(TeamCategory.BOYS).build();
        Module module = Module.builder().id(1L).skill(skill).build();
        when(moduleRepository.findById(1L)).thenReturn(Optional.of(module));
        when(teamRepository.findById(10L)).thenReturn(Optional.of(team));
        when(playerRepository.findById(5L)).thenReturn(Optional.of(Player.builder().id(5L).build()));
        when(playerModuleProgressRepository.findByModuleIdAndPlayerId(1L, 5L)).thenReturn(Optional.empty());
        when(playerModuleProgressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(playerModuleProgressMapper.toResponse(any()))
                .thenReturn(PlayerModuleProgressResponse.builder().build());

        PlayerModuleProgressRequest request = PlayerModuleProgressRequest.builder()
                .moduleId(1L)
                .playerId(5L)
                .teamId(10L)
                .status(ModuleProgressStatus.STARTED)
                .build();

        playerModuleProgressService.save(request, principal(UserRole.ADMIN, 1L));

        org.mockito.ArgumentCaptor<PlayerModuleProgress> captor =
                org.mockito.ArgumentCaptor.forClass(PlayerModuleProgress.class);
        org.mockito.Mockito.verify(playerModuleProgressRepository).save(captor.capture());
        assertThat(captor.getValue().getRecordedByCoach()).isNull();
        assertThat(captor.getValue().getStatus()).isEqualTo(ModuleProgressStatus.STARTED);
    }

    @Test
    void save_asCoachAssignedToTeam_attributesRecordToCoach() {
        Team team = Team.builder().id(10L).ageGroup(AgeGroup.U12).category(TeamCategory.BOYS).build();
        Skill skill = Skill.builder().id(1L).ageGroup(AgeGroup.U12).category(TeamCategory.BOYS).build();
        Module module = Module.builder().id(1L).skill(skill).build();
        when(moduleRepository.findById(1L)).thenReturn(Optional.of(module));
        when(teamRepository.findById(10L)).thenReturn(Optional.of(team));
        Coach coach = Coach.builder().id(20L).build();
        when(coachRepository.findByUserId(2L)).thenReturn(Optional.of(coach));
        when(coachTeamRepository.findByCoachId(20L))
                .thenReturn(List.of(CoachTeam.builder().team(team).status(CoachTeamStatus.ACTIVE).build()));
        when(playerRepository.findById(5L)).thenReturn(Optional.of(Player.builder().id(5L).build()));
        when(playerModuleProgressRepository.findByModuleIdAndPlayerId(1L, 5L)).thenReturn(Optional.empty());
        when(playerModuleProgressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(playerModuleProgressMapper.toResponse(any()))
                .thenReturn(PlayerModuleProgressResponse.builder().build());

        PlayerModuleProgressRequest request = PlayerModuleProgressRequest.builder()
                .moduleId(1L)
                .playerId(5L)
                .teamId(10L)
                .status(ModuleProgressStatus.COMPLETED)
                .build();

        playerModuleProgressService.save(request, principal(UserRole.COACH, 2L));

        org.mockito.ArgumentCaptor<PlayerModuleProgress> captor =
                org.mockito.ArgumentCaptor.forClass(PlayerModuleProgress.class);
        org.mockito.Mockito.verify(playerModuleProgressRepository).save(captor.capture());
        assertThat(captor.getValue().getRecordedByCoach()).isEqualTo(coach);
    }

    @Test
    void save_asCoachNotAssignedToTeam_isDenied() {
        Team team = Team.builder().id(10L).ageGroup(AgeGroup.U12).category(TeamCategory.BOYS).build();
        Skill skill = Skill.builder().id(1L).ageGroup(AgeGroup.U12).category(TeamCategory.BOYS).build();
        Module module = Module.builder().id(1L).skill(skill).build();
        when(moduleRepository.findById(1L)).thenReturn(Optional.of(module));
        when(teamRepository.findById(10L)).thenReturn(Optional.of(team));
        Coach coach = Coach.builder().id(20L).build();
        when(coachRepository.findByUserId(2L)).thenReturn(Optional.of(coach));
        when(coachTeamRepository.findByCoachId(20L)).thenReturn(List.of());

        PlayerModuleProgressRequest request = PlayerModuleProgressRequest.builder()
                .moduleId(1L)
                .playerId(5L)
                .teamId(10L)
                .status(ModuleProgressStatus.STARTED)
                .build();

        assertThatThrownBy(() -> playerModuleProgressService.save(request, principal(UserRole.COACH, 2L)))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void save_existingProgress_updatesRatherThanDuplicates() {
        Team team = Team.builder().id(10L).ageGroup(AgeGroup.U12).category(TeamCategory.BOYS).build();
        Skill skill = Skill.builder().id(1L).ageGroup(AgeGroup.U12).category(TeamCategory.BOYS).build();
        Module module = Module.builder().id(1L).skill(skill).build();
        when(moduleRepository.findById(1L)).thenReturn(Optional.of(module));
        when(teamRepository.findById(10L)).thenReturn(Optional.of(team));
        when(playerRepository.findById(5L)).thenReturn(Optional.of(Player.builder().id(5L).build()));
        PlayerModuleProgress existing = PlayerModuleProgress.builder()
                .id(99L)
                .module(module)
                .status(ModuleProgressStatus.STARTED)
                .build();
        when(playerModuleProgressRepository.findByModuleIdAndPlayerId(1L, 5L)).thenReturn(Optional.of(existing));
        when(playerModuleProgressRepository.save(existing)).thenReturn(existing);
        when(playerModuleProgressMapper.toResponse(existing))
                .thenReturn(PlayerModuleProgressResponse.builder().build());

        PlayerModuleProgressRequest request = PlayerModuleProgressRequest.builder()
                .moduleId(1L)
                .playerId(5L)
                .teamId(10L)
                .status(ModuleProgressStatus.COMPLETED)
                .build();

        playerModuleProgressService.save(request, principal(UserRole.ADMIN, 1L));

        assertThat(existing.getId()).isEqualTo(99L);
        assertThat(existing.getStatus()).isEqualTo(ModuleProgressStatus.COMPLETED);
        org.mockito.Mockito.verify(playerModuleProgressRepository, org.mockito.Mockito.never())
                .save(org.mockito.ArgumentMatchers.argThat(p -> p != existing));
    }

    @Test
    void saveBulk_entryModuleNotBelongingToSkill_throwsInvalidRequestException() {
        Team team = Team.builder().id(10L).ageGroup(AgeGroup.U12).category(TeamCategory.BOYS).build();
        Skill skill = Skill.builder().id(1L).ageGroup(AgeGroup.U12).category(TeamCategory.BOYS).build();
        Skill otherSkill = Skill.builder().id(2L).build();
        when(skillRepository.findById(1L)).thenReturn(Optional.of(skill));
        when(teamRepository.findById(10L)).thenReturn(Optional.of(team));
        when(playerRepository.findById(5L)).thenReturn(Optional.of(Player.builder().id(5L).build()));
        Module wrongModule = Module.builder().id(99L).skill(otherSkill).build();
        when(moduleRepository.findById(99L)).thenReturn(Optional.of(wrongModule));

        BulkPlayerModuleProgressRequest request = BulkPlayerModuleProgressRequest.builder()
                .skillId(1L)
                .playerId(5L)
                .teamId(10L)
                .entries(List.of(BulkPlayerModuleProgressEntry.builder()
                        .moduleId(99L)
                        .status(ModuleProgressStatus.STARTED)
                        .build()))
                .build();

        assertThatThrownBy(() -> playerModuleProgressService.saveBulk(request, principal(UserRole.ADMIN, 1L)))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void getPlayerTeamProgress_asParentNotOwningPlayer_isDenied() {
        Team team = Team.builder().id(10L).ageGroup(AgeGroup.U12).category(TeamCategory.BOYS).build();
        when(playerRepository.findById(5L)).thenReturn(Optional.of(Player.builder().id(5L).build()));
        when(teamRepository.findById(10L)).thenReturn(Optional.of(team));
        Parent parent = Parent.builder().id(30L).build();
        when(parentRepository.findByUserId(3L)).thenReturn(Optional.of(parent));
        when(playerParentRepository.findByParentId(30L)).thenReturn(List.of());

        assertThatThrownBy(
                        () -> playerModuleProgressService.getPlayerTeamProgress(5L, 10L, principal(UserRole.PARENT, 3L)))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getPlayerTeamProgress_asParentOwningPlayer_computesWeightedPercentage() {
        Team team = Team.builder().id(10L).ageGroup(AgeGroup.U12).category(TeamCategory.BOYS).build();
        when(playerRepository.findById(5L)).thenReturn(Optional.of(Player.builder().id(5L).build()));
        when(teamRepository.findById(10L)).thenReturn(Optional.of(team));
        Parent parent = Parent.builder().id(30L).build();
        when(parentRepository.findByUserId(3L)).thenReturn(Optional.of(parent));
        PlayerParent link = PlayerParent.builder()
                .player(Player.builder().id(5L).build())
                .build();
        when(playerParentRepository.findByParentId(30L)).thenReturn(List.of(link));

        Skill skillA = Skill.builder().id(1L).name("Dribbling").build();
        Skill skillB = Skill.builder().id(2L).name("Passing").build();
        when(skillRepository.findByAgeGroupAndCategory(AgeGroup.U12, TeamCategory.BOYS))
                .thenReturn(List.of(skillA, skillB));

        Module moduleA1 = Module.builder().id(11L).skill(skillA).name("Close control").build();
        Module moduleA2 = Module.builder().id(12L).skill(skillA).name("Change of direction").build();
        Module moduleB1 = Module.builder().id(21L).skill(skillB).name("Short passing").build();
        when(moduleRepository.findBySkillId(1L)).thenReturn(List.of(moduleA1, moduleA2));
        when(moduleRepository.findBySkillId(2L)).thenReturn(List.of(moduleB1));

        // Player is COMPLETED (100%) on both dribbling modules, NOT_STARTED (0%, no record) on
        // the one passing module. Skill A = 100%, Skill B = 0%.
        // Weighted overall = (100+100+0) / 3 modules = 66.7%, NOT the unweighted average of
        // skill percentages ((100+0)/2 = 50%).
        PlayerModuleProgress progressA1 = PlayerModuleProgress.builder()
                .module(moduleA1)
                .status(ModuleProgressStatus.COMPLETED)
                .build();
        PlayerModuleProgress progressA2 = PlayerModuleProgress.builder()
                .module(moduleA2)
                .status(ModuleProgressStatus.COMPLETED)
                .build();
        when(playerModuleProgressRepository.findByPlayerId(5L)).thenReturn(List.of(progressA1, progressA2));

        PlayerTeamProgressResponse response =
                playerModuleProgressService.getPlayerTeamProgress(5L, 10L, principal(UserRole.PARENT, 3L));

        assertThat(response.getTotalModules()).isEqualTo(3);
        assertThat(response.getOverallPercentage()).isEqualTo(66.7);
        assertThat(response.getSkills()).hasSize(2);
        assertThat(response.getSkills().get(0).getSkillPercentage()).isEqualTo(100.0);
        assertThat(response.getSkills().get(1).getSkillPercentage()).isEqualTo(0.0);
    }
}
