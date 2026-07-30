package com.ahmadisyraf39.spabs_v2.activity.service;

import com.ahmadisyraf39.spabs_v2.activity.dto.request.ActivityRequest;
import com.ahmadisyraf39.spabs_v2.activity.dto.request.RecurringActivityRequest;
import com.ahmadisyraf39.spabs_v2.activity.dto.response.ActivityResponse;
import com.ahmadisyraf39.spabs_v2.activity.entity.Activity;
import com.ahmadisyraf39.spabs_v2.activity.mapper.ActivityMapper;
import com.ahmadisyraf39.spabs_v2.activity.repository.ActivityRepository;
import com.ahmadisyraf39.spabs_v2.common.exception.InvalidRequestException;
import com.ahmadisyraf39.spabs_v2.common.exception.ResourceNotFoundException;
import com.ahmadisyraf39.spabs_v2.membership.entity.enums.CoachTeamStatus;
import com.ahmadisyraf39.spabs_v2.membership.entity.enums.PlayerTeamStatus;
import com.ahmadisyraf39.spabs_v2.membership.repository.CoachTeamRepository;
import com.ahmadisyraf39.spabs_v2.membership.repository.PlayerParentRepository;
import com.ahmadisyraf39.spabs_v2.membership.repository.PlayerTeamRepository;
import com.ahmadisyraf39.spabs_v2.security.UserPrincipal;
import com.ahmadisyraf39.spabs_v2.team.entity.Team;
import com.ahmadisyraf39.spabs_v2.team.repository.TeamRepository;
import com.ahmadisyraf39.spabs_v2.user.entity.Coach;
import com.ahmadisyraf39.spabs_v2.user.entity.Parent;
import com.ahmadisyraf39.spabs_v2.user.entity.enums.UserRole;
import com.ahmadisyraf39.spabs_v2.user.repository.CoachRepository;
import com.ahmadisyraf39.spabs_v2.user.repository.ParentRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final TeamRepository teamRepository;
    private final ActivityMapper activityMapper;
    private final CoachRepository coachRepository;
    private final ParentRepository parentRepository;
    private final CoachTeamRepository coachTeamRepository;
    private final PlayerParentRepository playerParentRepository;
    private final PlayerTeamRepository playerTeamRepository;

    public ActivityResponse create(ActivityRequest request) {
        Activity activity = activityMapper.toEntity(request);
        activity.setTeam(findTeamById(request.getTeamId()));
        return activityMapper.toResponse(activityRepository.save(activity));
    }

    public ActivityResponse getById(Long id) {
        return activityMapper.toResponse(findEntityById(id));
    }

    public List<ActivityResponse> getAll() {
        return activityRepository.findAll().stream().map(activityMapper::toResponse).toList();
    }

    public List<ActivityResponse> getByTeam(Long teamId) {
        return activityRepository.findByTeamId(teamId).stream()
                .map(activityMapper::toResponse)
                .toList();
    }

    public ActivityResponse update(Long id, ActivityRequest request) {
        Activity activity = findEntityById(id);
        activityMapper.updateEntity(activity, request);
        activity.setTeam(findTeamById(request.getTeamId()));
        return activityMapper.toResponse(activityRepository.save(activity));
    }

    public void delete(Long id) {
        activityRepository.delete(findEntityById(id));
    }

    private static final int MAX_RECURRING_ACTIVITIES = 500;

    public List<ActivityResponse> createRecurring(RecurringActivityRequest request) {
        List<Team> teams = resolveTeams(request);
        List<LocalDate> dates =
                datesMatchingDayOfWeek(request.getStartDate(), request.getEndDate(), request.getDayOfWeek());
        if (dates.isEmpty()) {
            throw new InvalidRequestException("No dates in range match the given day of week");
        }
        if ((long) teams.size() * dates.size() > MAX_RECURRING_ACTIVITIES) {
            throw new InvalidRequestException(
                    "This would create " + (teams.size() * dates.size()) + " activities, exceeding the limit of "
                            + MAX_RECURRING_ACTIVITIES + " — narrow the date range or team selection");
        }

        List<Activity> activities = new ArrayList<>();
        for (Team team : teams) {
            for (LocalDate date : dates) {
                activities.add(Activity.builder()
                        .team(team)
                        .type(request.getType())
                        .title(request.getTitle())
                        .startAt(date.atTime(request.getStartTime()))
                        .endAt(request.getEndTime() != null ? date.atTime(request.getEndTime()) : null)
                        .location(request.getLocation())
                        .description(request.getDescription())
                        .build());
            }
        }
        return activityRepository.saveAll(activities).stream()
                .map(activityMapper::toResponse)
                .toList();
    }

    private List<Team> resolveTeams(RecurringActivityRequest request) {
        if (Boolean.TRUE.equals(request.getApplyToAllTeams())) {
            return teamRepository.findAll();
        }
        if (request.getTeamIds() == null || request.getTeamIds().isEmpty()) {
            throw new InvalidRequestException("teamIds must be provided unless applyToAllTeams is true");
        }
        return request.getTeamIds().stream().map(this::findTeamById).toList();
    }

    private List<LocalDate> datesMatchingDayOfWeek(LocalDate startDate, LocalDate endDate, DayOfWeek dayOfWeek) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate cursor = startDate;
        while (!cursor.isAfter(endDate)) {
            if (cursor.getDayOfWeek() == dayOfWeek) {
                dates.add(cursor);
            }
            cursor = cursor.plusDays(1);
        }
        return dates;
    }

    public List<ActivityResponse> getUpcomingForCoach(Long coachId, UserPrincipal caller) {
        assertCanAccessCoach(caller, coachId);
        List<Long> teamIds = coachTeamRepository.findByCoachId(coachId).stream()
                .filter(ct -> ct.getStatus() == CoachTeamStatus.ACTIVE)
                .map(ct -> ct.getTeam().getId())
                .distinct()
                .toList();
        return upcomingForTeams(teamIds);
    }

    public List<ActivityResponse> getUpcomingForParent(Long parentId, UserPrincipal caller) {
        assertCanAccessParent(caller, parentId);
        List<Long> playerIds = playerParentRepository.findByParentId(parentId).stream()
                .map(pp -> pp.getPlayer().getId())
                .distinct()
                .toList();
        List<Long> teamIds = playerIds.stream()
                .flatMap(playerId -> playerTeamRepository.findByPlayerId(playerId).stream())
                .filter(pt -> pt.getStatus() == PlayerTeamStatus.ACTIVE)
                .map(pt -> pt.getTeam().getId())
                .distinct()
                .toList();
        return upcomingForTeams(teamIds);
    }

    public List<ActivityResponse> getMyUpcoming(UserPrincipal caller) {
        UserRole role = caller.getUser().getRole();
        if (role == UserRole.COACH) {
            Coach coach = findCoachByUserId(caller.getId());
            return getUpcomingForCoach(coach.getId(), caller);
        }
        if (role == UserRole.PARENT) {
            Parent parent = findParentByUserId(caller.getId());
            return getUpcomingForParent(parent.getId(), caller);
        }
        throw new AccessDeniedException("Only coaches and parents have a personal upcoming-activities view");
    }

    private List<ActivityResponse> upcomingForTeams(List<Long> teamIds) {
        if (teamIds.isEmpty()) {
            return List.of();
        }
        return activityRepository.findByTeamIdInAndStartAtAfter(teamIds, LocalDateTime.now()).stream()
                .map(activityMapper::toResponse)
                .toList();
    }

    private void assertCanAccessCoach(UserPrincipal caller, Long coachId) {
        UserRole role = caller.getUser().getRole();
        if (role == UserRole.ADMIN || role == UserRole.SUPER_ADMIN) {
            return;
        }
        if (role == UserRole.COACH && findCoachByUserId(caller.getId()).getId().equals(coachId)) {
            return;
        }
        throw new AccessDeniedException("Not allowed to view this coach's activities");
    }

    private void assertCanAccessParent(UserPrincipal caller, Long parentId) {
        UserRole role = caller.getUser().getRole();
        if (role == UserRole.ADMIN || role == UserRole.SUPER_ADMIN) {
            return;
        }
        if (role == UserRole.PARENT && findParentByUserId(caller.getId()).getId().equals(parentId)) {
            return;
        }
        throw new AccessDeniedException("Not allowed to view this parent's activities");
    }

    private Coach findCoachByUserId(Long userId) {
        return coachRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AccessDeniedException("No coach profile for this account"));
    }

    private Parent findParentByUserId(Long userId) {
        return parentRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AccessDeniedException("No parent profile for this account"));
    }

    private Activity findEntityById(Long id) {
        return activityRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found: " + id));
    }

    private Team findTeamById(Long teamId) {
        return teamRepository
                .findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + teamId));
    }
}
