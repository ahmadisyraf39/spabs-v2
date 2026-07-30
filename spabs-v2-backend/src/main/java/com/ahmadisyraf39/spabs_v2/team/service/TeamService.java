package com.ahmadisyraf39.spabs_v2.team.service;

import com.ahmadisyraf39.spabs_v2.common.exception.ResourceNotFoundException;
import com.ahmadisyraf39.spabs_v2.team.dto.request.TeamRequest;
import com.ahmadisyraf39.spabs_v2.team.dto.response.TeamResponse;
import com.ahmadisyraf39.spabs_v2.team.entity.Team;
import com.ahmadisyraf39.spabs_v2.team.mapper.TeamMapper;
import com.ahmadisyraf39.spabs_v2.team.repository.TeamRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMapper teamMapper;

    public TeamResponse create(TeamRequest request) {
        Team team = teamMapper.toEntity(request);
        return teamMapper.toResponse(teamRepository.save(team));
    }

    public TeamResponse getById(Long id) {
        return teamMapper.toResponse(findEntityById(id));
    }

    public List<TeamResponse> getAll() {
        return teamRepository.findAll().stream().map(teamMapper::toResponse).toList();
    }

    public TeamResponse update(Long id, TeamRequest request) {
        Team team = findEntityById(id);
        teamMapper.updateEntity(team, request);
        return teamMapper.toResponse(teamRepository.save(team));
    }

    public void delete(Long id) {
        teamRepository.delete(findEntityById(id));
    }

    private Team findEntityById(Long id) {
        return teamRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + id));
    }
}
