package com.ahmadisyraf39.spabs_v2.membership.service;

import com.ahmadisyraf39.spabs_v2.common.exception.ResourceNotFoundException;
import com.ahmadisyraf39.spabs_v2.membership.dto.request.CoachTeamRequest;
import com.ahmadisyraf39.spabs_v2.membership.dto.response.CoachTeamResponse;
import com.ahmadisyraf39.spabs_v2.membership.entity.CoachTeam;
import com.ahmadisyraf39.spabs_v2.membership.mapper.CoachTeamMapper;
import com.ahmadisyraf39.spabs_v2.membership.repository.CoachTeamRepository;
import com.ahmadisyraf39.spabs_v2.team.entity.Team;
import com.ahmadisyraf39.spabs_v2.team.repository.TeamRepository;
import com.ahmadisyraf39.spabs_v2.user.entity.Coach;
import com.ahmadisyraf39.spabs_v2.user.repository.CoachRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CoachTeamService {

    private final CoachTeamRepository coachTeamRepository;
    private final CoachRepository coachRepository;
    private final TeamRepository teamRepository;
    private final CoachTeamMapper coachTeamMapper;

    public CoachTeamResponse create(CoachTeamRequest request) {
        CoachTeam coachTeam = coachTeamMapper.toEntity(request);
        coachTeam.setCoach(findCoachById(request.getCoachId()));
        coachTeam.setTeam(findTeamById(request.getTeamId()));
        return coachTeamMapper.toResponse(coachTeamRepository.save(coachTeam));
    }

    public CoachTeamResponse getById(Long id) {
        return coachTeamMapper.toResponse(findEntityById(id));
    }

    public List<CoachTeamResponse> getAll() {
        return coachTeamRepository.findAll().stream().map(coachTeamMapper::toResponse).toList();
    }

    public List<CoachTeamResponse> getByCoach(Long coachId) {
        return coachTeamRepository.findByCoachId(coachId).stream().map(coachTeamMapper::toResponse).toList();
    }

    public List<CoachTeamResponse> getByTeam(Long teamId) {
        return coachTeamRepository.findByTeamId(teamId).stream().map(coachTeamMapper::toResponse).toList();
    }

    public CoachTeamResponse update(Long id, CoachTeamRequest request) {
        CoachTeam coachTeam = findEntityById(id);
        coachTeamMapper.updateEntity(coachTeam, request);
        coachTeam.setCoach(findCoachById(request.getCoachId()));
        coachTeam.setTeam(findTeamById(request.getTeamId()));
        return coachTeamMapper.toResponse(coachTeamRepository.save(coachTeam));
    }

    public void delete(Long id) {
        coachTeamRepository.delete(findEntityById(id));
    }

    private CoachTeam findEntityById(Long id) {
        return coachTeamRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CoachTeam not found: " + id));
    }

    private Coach findCoachById(Long coachId) {
        return coachRepository
                .findById(coachId)
                .orElseThrow(() -> new ResourceNotFoundException("Coach not found: " + coachId));
    }

    private Team findTeamById(Long teamId) {
        return teamRepository
                .findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + teamId));
    }
}
