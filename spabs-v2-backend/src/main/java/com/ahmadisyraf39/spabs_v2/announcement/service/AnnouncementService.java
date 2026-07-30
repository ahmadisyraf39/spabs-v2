package com.ahmadisyraf39.spabs_v2.announcement.service;

import com.ahmadisyraf39.spabs_v2.announcement.dto.request.AnnouncementRequest;
import com.ahmadisyraf39.spabs_v2.announcement.dto.response.AnnouncementResponse;
import com.ahmadisyraf39.spabs_v2.announcement.entity.Announcement;
import com.ahmadisyraf39.spabs_v2.announcement.mapper.AnnouncementMapper;
import com.ahmadisyraf39.spabs_v2.announcement.repository.AnnouncementRepository;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final TeamRepository teamRepository;
    private final AnnouncementMapper announcementMapper;
    private final CoachRepository coachRepository;
    private final ParentRepository parentRepository;
    private final CoachTeamRepository coachTeamRepository;
    private final PlayerParentRepository playerParentRepository;
    private final PlayerTeamRepository playerTeamRepository;

    public AnnouncementResponse create(AnnouncementRequest request, UserPrincipal caller) {
        Announcement announcement = announcementMapper.toEntity(request);
        announcement.setTeam(request.getTeamId() != null ? findTeamById(request.getTeamId()) : null);
        announcement.setCreatedBy(caller.getUser());
        return announcementMapper.toResponse(announcementRepository.save(announcement));
    }

    public AnnouncementResponse getById(Long id) {
        return announcementMapper.toResponse(findEntityById(id));
    }

    public List<AnnouncementResponse> getAll() {
        return announcementRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(announcementMapper::toResponse)
                .toList();
    }

    public List<AnnouncementResponse> getByTeam(Long teamId) {
        return announcementRepository.findByTeamId(teamId).stream()
                .map(announcementMapper::toResponse)
                .toList();
    }

    public AnnouncementResponse update(Long id, AnnouncementRequest request) {
        Announcement announcement = findEntityById(id);
        announcementMapper.updateEntity(announcement, request);
        announcement.setTeam(request.getTeamId() != null ? findTeamById(request.getTeamId()) : null);
        return announcementMapper.toResponse(announcementRepository.save(announcement));
    }

    public void delete(Long id) {
        announcementRepository.delete(findEntityById(id));
    }

    public List<AnnouncementResponse> getMine(UserPrincipal caller) {
        UserRole role = caller.getUser().getRole();
        if (role == UserRole.ADMIN || role == UserRole.SUPER_ADMIN) {
            return getAll();
        }

        List<Long> teamIds;
        if (role == UserRole.COACH) {
            Coach coach = findCoachByUserId(caller.getId());
            teamIds = coachTeamRepository.findByCoachId(coach.getId()).stream()
                    .filter(ct -> ct.getStatus() == CoachTeamStatus.ACTIVE)
                    .map(ct -> ct.getTeam().getId())
                    .distinct()
                    .toList();
        } else if (role == UserRole.PARENT) {
            Parent parent = findParentByUserId(caller.getId());
            List<Long> playerIds = playerParentRepository.findByParentId(parent.getId()).stream()
                    .map(pp -> pp.getPlayer().getId())
                    .distinct()
                    .toList();
            teamIds = playerIds.stream()
                    .flatMap(playerId -> playerTeamRepository.findByPlayerId(playerId).stream())
                    .filter(pt -> pt.getStatus() == PlayerTeamStatus.ACTIVE)
                    .map(pt -> pt.getTeam().getId())
                    .distinct()
                    .toList();
        } else {
            throw new AccessDeniedException("Not allowed to view announcements");
        }

        List<Announcement> results = new ArrayList<>(announcementRepository.findByTeamIdIsNull());
        if (!teamIds.isEmpty()) {
            results.addAll(announcementRepository.findByTeamIdIn(teamIds));
        }
        return results.stream()
                .sorted(Comparator.comparing(Announcement::getCreatedAt).reversed())
                .map(announcementMapper::toResponse)
                .toList();
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

    private Announcement findEntityById(Long id) {
        return announcementRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found: " + id));
    }

    private Team findTeamById(Long teamId) {
        return teamRepository
                .findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + teamId));
    }
}
