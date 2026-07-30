package com.ahmadisyraf39.spabs_v2.progress.service;

import com.ahmadisyraf39.spabs_v2.common.exception.InvalidRequestException;
import com.ahmadisyraf39.spabs_v2.common.exception.ResourceNotFoundException;
import com.ahmadisyraf39.spabs_v2.membership.entity.enums.CoachTeamStatus;
import com.ahmadisyraf39.spabs_v2.membership.repository.CoachTeamRepository;
import com.ahmadisyraf39.spabs_v2.membership.repository.PlayerParentRepository;
import com.ahmadisyraf39.spabs_v2.player.entity.Player;
import com.ahmadisyraf39.spabs_v2.player.repository.PlayerRepository;
import com.ahmadisyraf39.spabs_v2.progress.dto.request.BulkPlayerModuleProgressEntry;
import com.ahmadisyraf39.spabs_v2.progress.dto.request.BulkPlayerModuleProgressRequest;
import com.ahmadisyraf39.spabs_v2.progress.dto.request.PlayerModuleProgressRequest;
import com.ahmadisyraf39.spabs_v2.progress.dto.response.ModuleProgressResponse;
import com.ahmadisyraf39.spabs_v2.progress.dto.response.PlayerModuleProgressResponse;
import com.ahmadisyraf39.spabs_v2.progress.dto.response.PlayerTeamProgressResponse;
import com.ahmadisyraf39.spabs_v2.progress.dto.response.SkillProgressResponse;
import com.ahmadisyraf39.spabs_v2.progress.entity.Module;
import com.ahmadisyraf39.spabs_v2.progress.entity.PlayerModuleProgress;
import com.ahmadisyraf39.spabs_v2.progress.entity.Skill;
import com.ahmadisyraf39.spabs_v2.progress.entity.enums.ModuleProgressStatus;
import com.ahmadisyraf39.spabs_v2.progress.mapper.PlayerModuleProgressMapper;
import com.ahmadisyraf39.spabs_v2.progress.repository.ModuleRepository;
import com.ahmadisyraf39.spabs_v2.progress.repository.PlayerModuleProgressRepository;
import com.ahmadisyraf39.spabs_v2.progress.repository.SkillRepository;
import com.ahmadisyraf39.spabs_v2.team.entity.Team;
import com.ahmadisyraf39.spabs_v2.team.repository.TeamRepository;
import com.ahmadisyraf39.spabs_v2.user.entity.Coach;
import com.ahmadisyraf39.spabs_v2.user.entity.Parent;
import com.ahmadisyraf39.spabs_v2.user.entity.enums.UserRole;
import com.ahmadisyraf39.spabs_v2.user.repository.CoachRepository;
import com.ahmadisyraf39.spabs_v2.user.repository.ParentRepository;
import com.ahmadisyraf39.spabs_v2.security.UserPrincipal;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlayerModuleProgressService {

    private final PlayerModuleProgressRepository playerModuleProgressRepository;
    private final ModuleRepository moduleRepository;
    private final SkillRepository skillRepository;
    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;
    private final CoachRepository coachRepository;
    private final CoachTeamRepository coachTeamRepository;
    private final ParentRepository parentRepository;
    private final PlayerParentRepository playerParentRepository;
    private final PlayerModuleProgressMapper playerModuleProgressMapper;

    public PlayerModuleProgressResponse save(PlayerModuleProgressRequest request, UserPrincipal caller) {
        Module module = findModuleById(request.getModuleId());
        Team team = findTeamById(request.getTeamId());
        validateTeamMatchesSkill(team, module.getSkill());
        Coach recordedBy = assertCanRecordProgress(caller, team);
        Player player = findPlayerById(request.getPlayerId());
        PlayerModuleProgress progress = upsert(module, player, request.getStatus(), recordedBy);
        return playerModuleProgressMapper.toResponse(progress);
    }

    public List<PlayerModuleProgressResponse> saveBulk(BulkPlayerModuleProgressRequest request, UserPrincipal caller) {
        Skill skill = findSkillById(request.getSkillId());
        Team team = findTeamById(request.getTeamId());
        validateTeamMatchesSkill(team, skill);
        Coach recordedBy = assertCanRecordProgress(caller, team);
        Player player = findPlayerById(request.getPlayerId());

        return request.getEntries().stream()
                .map(entry -> upsertBulkEntry(skill, player, entry, recordedBy))
                .map(playerModuleProgressMapper::toResponse)
                .toList();
    }

    public void delete(Long id) {
        playerModuleProgressRepository.delete(findEntityById(id));
    }

    public PlayerTeamProgressResponse getPlayerTeamProgress(Long playerId, Long teamId, UserPrincipal caller) {
        findPlayerById(playerId);
        Team team = findTeamById(teamId);
        assertCanViewProgress(caller, playerId, team);

        List<Skill> skills = skillRepository.findByAgeGroupAndCategory(team.getAgeGroup(), team.getCategory());
        List<PlayerModuleProgress> playerProgress = playerModuleProgressRepository.findByPlayerId(playerId);

        List<SkillProgressResponse> skillResponses = new ArrayList<>();
        int totalModules = 0;
        long overallPercentageSum = 0;

        for (Skill skill : skills) {
            List<Module> modules = moduleRepository.findBySkillId(skill.getId());
            List<ModuleProgressResponse> moduleResponses = new ArrayList<>();
            long skillPercentageSum = 0;

            for (Module module : modules) {
                ModuleProgressStatus status = playerProgress.stream()
                        .filter(p -> p.getModule().getId().equals(module.getId()))
                        .findFirst()
                        .map(PlayerModuleProgress::getStatus)
                        .orElse(ModuleProgressStatus.NOT_STARTED);
                int percentage = status.getPercentage();
                moduleResponses.add(ModuleProgressResponse.builder()
                        .moduleId(module.getId())
                        .moduleName(module.getName())
                        .status(status)
                        .percentage(percentage)
                        .build());
                skillPercentageSum += percentage;
            }

            double skillPercentage = modules.isEmpty() ? 0.0 : roundToOneDecimal(skillPercentageSum, modules.size());
            skillResponses.add(SkillProgressResponse.builder()
                    .skillId(skill.getId())
                    .skillName(skill.getName())
                    .modules(moduleResponses)
                    .skillPercentage(skillPercentage)
                    .build());

            totalModules += modules.size();
            overallPercentageSum += skillPercentageSum;
        }

        double overallPercentage = totalModules == 0 ? 0.0 : roundToOneDecimal(overallPercentageSum, totalModules);

        return PlayerTeamProgressResponse.builder()
                .playerId(playerId)
                .teamId(teamId)
                .skills(skillResponses)
                .totalModules(totalModules)
                .overallPercentage(overallPercentage)
                .build();
    }

    private double roundToOneDecimal(long sum, int count) {
        return Math.round(sum * 10.0 / count) / 10.0;
    }

    private PlayerModuleProgress upsertBulkEntry(
            Skill skill, Player player, BulkPlayerModuleProgressEntry entry, Coach recordedBy) {
        Module module = findModuleById(entry.getModuleId());
        if (!module.getSkill().getId().equals(skill.getId())) {
            throw new InvalidRequestException(
                    "Module " + entry.getModuleId() + " does not belong to skill " + skill.getId());
        }
        return upsert(module, player, entry.getStatus(), recordedBy);
    }

    private PlayerModuleProgress upsert(Module module, Player player, ModuleProgressStatus status, Coach recordedBy) {
        PlayerModuleProgress progress = playerModuleProgressRepository
                .findByModuleIdAndPlayerId(module.getId(), player.getId())
                .orElseGet(PlayerModuleProgress::new);
        if (progress.getId() == null) {
            progress.setModule(module);
            progress.setPlayer(player);
        }
        progress.setStatus(status);
        progress.setRecordedByCoach(recordedBy);
        return playerModuleProgressRepository.save(progress);
    }

    private void validateTeamMatchesSkill(Team team, Skill skill) {
        if (team.getAgeGroup() != skill.getAgeGroup() || team.getCategory() != skill.getCategory()) {
            throw new InvalidRequestException(
                    "Team " + team.getId() + " does not match this skill's age group/category");
        }
    }

    private Coach assertCanRecordProgress(UserPrincipal caller, Team team) {
        UserRole role = caller.getUser().getRole();
        if (role == UserRole.ADMIN || role == UserRole.SUPER_ADMIN) {
            return null;
        }
        if (role == UserRole.COACH) {
            Coach coach = findCoachByUserId(caller.getId());
            boolean assigned = coachTeamRepository.findByCoachId(coach.getId()).stream()
                    .anyMatch(ct -> ct.getStatus() == CoachTeamStatus.ACTIVE
                            && ct.getTeam().getId().equals(team.getId()));
            if (!assigned) {
                throw new AccessDeniedException("Not assigned to this team");
            }
            return coach;
        }
        throw new AccessDeniedException("Not allowed to record progress");
    }

    private void assertCanViewProgress(UserPrincipal caller, Long playerId, Team team) {
        UserRole role = caller.getUser().getRole();
        if (role == UserRole.ADMIN || role == UserRole.SUPER_ADMIN) {
            return;
        }
        if (role == UserRole.COACH) {
            Coach coach = findCoachByUserId(caller.getId());
            boolean assigned = coachTeamRepository.findByCoachId(coach.getId()).stream()
                    .anyMatch(ct -> ct.getStatus() == CoachTeamStatus.ACTIVE
                            && ct.getTeam().getId().equals(team.getId()));
            if (assigned) {
                return;
            }
            throw new AccessDeniedException("Not assigned to this team");
        }
        if (role == UserRole.PARENT) {
            Parent parent = parentRepository
                    .findByUserId(caller.getId())
                    .orElseThrow(() -> new AccessDeniedException("No parent profile for this account"));
            boolean isOwnChild = playerParentRepository.findByParentId(parent.getId()).stream()
                    .anyMatch(pp -> pp.getPlayer().getId().equals(playerId));
            if (isOwnChild) {
                return;
            }
            throw new AccessDeniedException("Not allowed to view this player's progress");
        }
        throw new AccessDeniedException("Not allowed to view this player's progress");
    }

    private Coach findCoachByUserId(Long userId) {
        return coachRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AccessDeniedException("No coach profile for this account"));
    }

    private PlayerModuleProgress findEntityById(Long id) {
        return playerModuleProgressRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PlayerModuleProgress not found: " + id));
    }

    private Module findModuleById(Long moduleId) {
        return moduleRepository
                .findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found: " + moduleId));
    }

    private Skill findSkillById(Long skillId) {
        return skillRepository
                .findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found: " + skillId));
    }

    private Player findPlayerById(Long playerId) {
        return playerRepository
                .findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found: " + playerId));
    }

    private Team findTeamById(Long teamId) {
        return teamRepository
                .findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + teamId));
    }
}
