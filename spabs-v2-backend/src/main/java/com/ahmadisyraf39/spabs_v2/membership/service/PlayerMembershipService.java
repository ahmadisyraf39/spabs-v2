package com.ahmadisyraf39.spabs_v2.membership.service;

import com.ahmadisyraf39.spabs_v2.common.exception.ResourceNotFoundException;
import com.ahmadisyraf39.spabs_v2.membership.dto.request.PlayerMembershipRequest;
import com.ahmadisyraf39.spabs_v2.membership.dto.response.PlayerMembershipResponse;
import com.ahmadisyraf39.spabs_v2.membership.entity.PlayerMembership;
import com.ahmadisyraf39.spabs_v2.membership.mapper.PlayerMembershipMapper;
import com.ahmadisyraf39.spabs_v2.membership.repository.PlayerMembershipRepository;
import com.ahmadisyraf39.spabs_v2.player.entity.Player;
import com.ahmadisyraf39.spabs_v2.player.repository.PlayerRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlayerMembershipService {

    private final PlayerMembershipRepository playerMembershipRepository;
    private final PlayerRepository playerRepository;
    private final PlayerMembershipMapper playerMembershipMapper;

    public PlayerMembershipResponse create(PlayerMembershipRequest request) {
        PlayerMembership playerMembership = playerMembershipMapper.toEntity(request);
        playerMembership.setPlayer(findPlayerById(request.getPlayerId()));
        return playerMembershipMapper.toResponse(playerMembershipRepository.save(playerMembership));
    }

    public PlayerMembershipResponse getById(Long id) {
        return playerMembershipMapper.toResponse(findEntityById(id));
    }

    public List<PlayerMembershipResponse> getAll() {
        return playerMembershipRepository.findAll().stream()
                .map(playerMembershipMapper::toResponse)
                .toList();
    }

    public List<PlayerMembershipResponse> getByPlayer(Long playerId) {
        return playerMembershipRepository.findByPlayerId(playerId).stream()
                .map(playerMembershipMapper::toResponse)
                .toList();
    }

    public PlayerMembershipResponse update(Long id, PlayerMembershipRequest request) {
        PlayerMembership playerMembership = findEntityById(id);
        playerMembershipMapper.updateEntity(playerMembership, request);
        playerMembership.setPlayer(findPlayerById(request.getPlayerId()));
        return playerMembershipMapper.toResponse(playerMembershipRepository.save(playerMembership));
    }

    public void delete(Long id) {
        playerMembershipRepository.delete(findEntityById(id));
    }

    private PlayerMembership findEntityById(Long id) {
        return playerMembershipRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PlayerMembership not found: " + id));
    }

    private Player findPlayerById(Long playerId) {
        return playerRepository
                .findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found: " + playerId));
    }
}
