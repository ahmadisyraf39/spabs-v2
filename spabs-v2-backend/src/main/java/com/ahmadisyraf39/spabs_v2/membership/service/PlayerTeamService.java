package com.ahmadisyraf39.spabs_v2.membership.service;

import com.ahmadisyraf39.spabs_v2.common.exception.DuplicateResourceException;
import com.ahmadisyraf39.spabs_v2.common.exception.ResourceNotFoundException;
import com.ahmadisyraf39.spabs_v2.membership.dto.request.PlayerTeamRequest;
import com.ahmadisyraf39.spabs_v2.membership.dto.response.PlayerTeamResponse;
import com.ahmadisyraf39.spabs_v2.membership.entity.PlayerTeam;
import com.ahmadisyraf39.spabs_v2.membership.entity.enums.PlayerTeamStatus;
import com.ahmadisyraf39.spabs_v2.membership.mapper.PlayerTeamMapper;
import com.ahmadisyraf39.spabs_v2.membership.repository.PlayerTeamRepository;
import com.ahmadisyraf39.spabs_v2.player.entity.Player;
import com.ahmadisyraf39.spabs_v2.player.repository.PlayerRepository;
import com.ahmadisyraf39.spabs_v2.team.entity.Team;
import com.ahmadisyraf39.spabs_v2.team.repository.TeamRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlayerTeamService {

    private final PlayerTeamRepository playerTeamRepository;
    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;
    private final PlayerTeamMapper playerTeamMapper;

    public PlayerTeamResponse create(PlayerTeamRequest request) {
        checkJerseyNumberAvailable(request, null);
        PlayerTeam playerTeam = playerTeamMapper.toEntity(request);
        playerTeam.setPlayer(findPlayerById(request.getPlayerId()));
        playerTeam.setTeam(findTeamById(request.getTeamId()));
        return playerTeamMapper.toResponse(playerTeamRepository.save(playerTeam));
    }

    public PlayerTeamResponse getById(Long id) {
        return playerTeamMapper.toResponse(findEntityById(id));
    }

    public List<PlayerTeamResponse> getAll() {
        return playerTeamRepository.findAll().stream().map(playerTeamMapper::toResponse).toList();
    }

    public List<PlayerTeamResponse> getByPlayer(Long playerId) {
        return playerTeamRepository.findByPlayerId(playerId).stream()
                .map(playerTeamMapper::toResponse)
                .toList();
    }

    public List<PlayerTeamResponse> getByTeam(Long teamId) {
        return playerTeamRepository.findByTeamId(teamId).stream().map(playerTeamMapper::toResponse).toList();
    }

    public PlayerTeamResponse update(Long id, PlayerTeamRequest request) {
        checkJerseyNumberAvailable(request, id);
        PlayerTeam playerTeam = findEntityById(id);
        playerTeamMapper.updateEntity(playerTeam, request);
        playerTeam.setPlayer(findPlayerById(request.getPlayerId()));
        playerTeam.setTeam(findTeamById(request.getTeamId()));
        return playerTeamMapper.toResponse(playerTeamRepository.save(playerTeam));
    }

    public void delete(Long id) {
        playerTeamRepository.delete(findEntityById(id));
    }

    private void checkJerseyNumberAvailable(PlayerTeamRequest request, Long excludeId) {
        if (request.getJerseyNumber() == null || request.getStatus() != PlayerTeamStatus.ACTIVE) {
            return;
        }
        playerTeamRepository
                .findByTeamIdAndJerseyNumberAndStatus(
                        request.getTeamId(), request.getJerseyNumber(), PlayerTeamStatus.ACTIVE)
                .filter(existing -> excludeId == null || !existing.getId().equals(excludeId))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException(
                            "Jersey number " + request.getJerseyNumber()
                                    + " is already assigned to an active player on this team");
                });
    }

    private PlayerTeam findEntityById(Long id) {
        return playerTeamRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PlayerTeam not found: " + id));
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
