package com.ahmadisyraf39.spabs_v2.membership.service;

import com.ahmadisyraf39.spabs_v2.common.exception.ResourceNotFoundException;
import com.ahmadisyraf39.spabs_v2.membership.dto.request.PlayerParentRequest;
import com.ahmadisyraf39.spabs_v2.membership.dto.response.PlayerParentResponse;
import com.ahmadisyraf39.spabs_v2.membership.entity.PlayerParent;
import com.ahmadisyraf39.spabs_v2.membership.mapper.PlayerParentMapper;
import com.ahmadisyraf39.spabs_v2.membership.repository.PlayerParentRepository;
import com.ahmadisyraf39.spabs_v2.player.entity.Player;
import com.ahmadisyraf39.spabs_v2.player.repository.PlayerRepository;
import com.ahmadisyraf39.spabs_v2.user.entity.Parent;
import com.ahmadisyraf39.spabs_v2.user.repository.ParentRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlayerParentService {

    private final PlayerParentRepository playerParentRepository;
    private final PlayerRepository playerRepository;
    private final ParentRepository parentRepository;
    private final PlayerParentMapper playerParentMapper;

    public PlayerParentResponse create(PlayerParentRequest request) {
        PlayerParent playerParent = playerParentMapper.toEntity(request);
        playerParent.setPlayer(findPlayerById(request.getPlayerId()));
        playerParent.setParent(findParentById(request.getParentId()));
        return playerParentMapper.toResponse(playerParentRepository.save(playerParent));
    }

    public PlayerParentResponse getById(Long id) {
        return playerParentMapper.toResponse(findEntityById(id));
    }

    public List<PlayerParentResponse> getAll() {
        return playerParentRepository.findAll().stream().map(playerParentMapper::toResponse).toList();
    }

    public List<PlayerParentResponse> getByPlayer(Long playerId) {
        return playerParentRepository.findByPlayerId(playerId).stream()
                .map(playerParentMapper::toResponse)
                .toList();
    }

    public List<PlayerParentResponse> getByParent(Long parentId) {
        return playerParentRepository.findByParentId(parentId).stream()
                .map(playerParentMapper::toResponse)
                .toList();
    }

    public PlayerParentResponse update(Long id, PlayerParentRequest request) {
        PlayerParent playerParent = findEntityById(id);
        playerParentMapper.updateEntity(playerParent, request);
        playerParent.setPlayer(findPlayerById(request.getPlayerId()));
        playerParent.setParent(findParentById(request.getParentId()));
        return playerParentMapper.toResponse(playerParentRepository.save(playerParent));
    }

    public void delete(Long id) {
        playerParentRepository.delete(findEntityById(id));
    }

    private PlayerParent findEntityById(Long id) {
        return playerParentRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PlayerParent not found: " + id));
    }

    private Player findPlayerById(Long playerId) {
        return playerRepository
                .findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found: " + playerId));
    }

    private Parent findParentById(Long parentId) {
        return parentRepository
                .findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found: " + parentId));
    }
}
