package com.ahmadisyraf39.spabs_v2.attendance.service;

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
import com.ahmadisyraf39.spabs_v2.common.exception.ResourceNotFoundException;
import com.ahmadisyraf39.spabs_v2.membership.entity.enums.CoachTeamStatus;
import com.ahmadisyraf39.spabs_v2.membership.repository.CoachTeamRepository;
import com.ahmadisyraf39.spabs_v2.player.entity.Player;
import com.ahmadisyraf39.spabs_v2.player.repository.PlayerRepository;
import com.ahmadisyraf39.spabs_v2.security.UserPrincipal;
import com.ahmadisyraf39.spabs_v2.team.repository.TeamRepository;
import com.ahmadisyraf39.spabs_v2.user.entity.Coach;
import com.ahmadisyraf39.spabs_v2.user.entity.enums.UserRole;
import com.ahmadisyraf39.spabs_v2.user.repository.CoachRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final ActivityRepository activityRepository;
    private final PlayerRepository playerRepository;
    private final AttendanceMapper attendanceMapper;
    private final CoachRepository coachRepository;
    private final CoachTeamRepository coachTeamRepository;
    private final TeamRepository teamRepository;

    @Value("${app.attendance.edit-window-days}")
    private long editWindowDays;

    public AttendanceResponse create(AttendanceRequest request, UserPrincipal caller) {
        Activity activity = findActivityById(request.getActivityId());
        assertCanRecordAttendance(caller, activity);
        checkNotDuplicate(request.getActivityId(), request.getPlayerId(), null);
        Attendance attendance = attendanceMapper.toEntity(request);
        attendance.setActivity(activity);
        attendance.setPlayer(findPlayerById(request.getPlayerId()));
        return attendanceMapper.toResponse(attendanceRepository.save(attendance));
    }

    public AttendanceResponse getById(Long id) {
        return attendanceMapper.toResponse(findEntityById(id));
    }

    public List<AttendanceResponse> getAll() {
        return attendanceRepository.findAll().stream().map(attendanceMapper::toResponse).toList();
    }

    public List<AttendanceResponse> getByActivity(Long activityId) {
        return attendanceRepository.findByActivityId(activityId).stream()
                .map(attendanceMapper::toResponse)
                .toList();
    }

    public List<AttendanceResponse> getByPlayer(Long playerId) {
        return attendanceRepository.findByPlayerId(playerId).stream()
                .map(attendanceMapper::toResponse)
                .toList();
    }

    public AttendanceResponse update(Long id, AttendanceRequest request, UserPrincipal caller) {
        Attendance attendance = findEntityById(id);
        Activity activity = findActivityById(request.getActivityId());
        assertCanRecordAttendance(caller, activity);
        checkNotDuplicate(request.getActivityId(), request.getPlayerId(), id);
        attendanceMapper.updateEntity(attendance, request);
        attendance.setActivity(activity);
        attendance.setPlayer(findPlayerById(request.getPlayerId()));
        return attendanceMapper.toResponse(attendanceRepository.save(attendance));
    }

    public void delete(Long id) {
        attendanceRepository.delete(findEntityById(id));
    }

    public List<AttendanceResponse> saveBulk(BulkAttendanceRequest request, UserPrincipal caller) {
        Activity activity = findActivityById(request.getActivityId());
        assertCanRecordAttendance(caller, activity);
        return request.getEntries().stream()
                .map(entry -> upsert(activity, entry))
                .map(attendanceMapper::toResponse)
                .toList();
    }

    private Attendance upsert(Activity activity, BulkAttendanceEntry entry) {
        Attendance attendance = attendanceRepository
                .findByActivityIdAndPlayerId(activity.getId(), entry.getPlayerId())
                .orElseGet(Attendance::new);
        if (attendance.getId() == null) {
            attendance.setActivity(activity);
            attendance.setPlayer(findPlayerById(entry.getPlayerId()));
        }
        attendance.setStatus(entry.getStatus());
        attendance.setNotes(entry.getNotes());
        return attendanceRepository.save(attendance);
    }

    public AttendanceSummaryResponse getSummary(Long playerId, Long teamId) {
        findPlayerById(playerId);
        findTeamById(teamId);

        List<Attendance> records = attendanceRepository.findByPlayerId(playerId).stream()
                .filter(a -> a.getActivity().getTeam().getId().equals(teamId))
                .toList();

        long present = records.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
                .count();
        long late =
                records.stream().filter(a -> a.getStatus() == AttendanceStatus.LATE).count();
        long absent = records.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.ABSENT)
                .count();
        long excused = records.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.EXCUSED)
                .count();

        long consideredTotal = records.size() - excused;
        double percentage =
                consideredTotal == 0 ? 0.0 : Math.round((present + late) * 1000.0 / consideredTotal) / 10.0;

        return AttendanceSummaryResponse.builder()
                .playerId(playerId)
                .teamId(teamId)
                .totalRecords(records.size())
                .presentCount(present)
                .lateCount(late)
                .absentCount(absent)
                .excusedCount(excused)
                .attendancePercentage(percentage)
                .records(records.stream().map(attendanceMapper::toResponse).toList())
                .build();
    }

    private void checkNotDuplicate(Long activityId, Long playerId, Long excludeId) {
        attendanceRepository
                .findByActivityIdAndPlayerId(activityId, playerId)
                .filter(existing -> excludeId == null || !existing.getId().equals(excludeId))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException(
                            "Player " + playerId + " already has an attendance record for activity " + activityId);
                });
    }

    private void assertCanRecordAttendance(UserPrincipal caller, Activity activity) {
        UserRole role = caller.getUser().getRole();
        if (role == UserRole.ADMIN || role == UserRole.SUPER_ADMIN) {
            return;
        }
        if (role == UserRole.COACH) {
            Coach coach = coachRepository
                    .findByUserId(caller.getId())
                    .orElseThrow(() -> new AccessDeniedException("No coach profile for this account"));
            boolean assigned = coachTeamRepository.findByCoachId(coach.getId()).stream()
                    .anyMatch(ct -> ct.getStatus() == CoachTeamStatus.ACTIVE
                            && ct.getTeam().getId().equals(activity.getTeam().getId()));
            if (!assigned) {
                throw new AccessDeniedException("Not assigned to this activity's team");
            }
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime windowEnd = activity.getStartAt().plusDays(editWindowDays);
            if (now.isBefore(activity.getStartAt()) || now.isAfter(windowEnd)) {
                throw new AccessDeniedException(
                        "Attendance can only be recorded between the activity start time and " + editWindowDays
                                + " days after");
            }
            return;
        }
        throw new AccessDeniedException("Not allowed to record attendance");
    }

    private Attendance findEntityById(Long id) {
        return attendanceRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance not found: " + id));
    }

    private Activity findActivityById(Long activityId) {
        return activityRepository
                .findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found: " + activityId));
    }

    private Player findPlayerById(Long playerId) {
        return playerRepository
                .findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found: " + playerId));
    }

    private void findTeamById(Long teamId) {
        if (!teamRepository.existsById(teamId)) {
            throw new ResourceNotFoundException("Team not found: " + teamId);
        }
    }
}
