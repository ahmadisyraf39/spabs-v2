package com.ahmadisyraf39.spabs_v2.player.service;

import com.ahmadisyraf39.spabs_v2.common.exception.ResourceNotFoundException;
import com.ahmadisyraf39.spabs_v2.player.dto.request.PlayerRequest;
import com.ahmadisyraf39.spabs_v2.player.dto.response.PlayerResponse;
import com.ahmadisyraf39.spabs_v2.player.entity.Player;
import com.ahmadisyraf39.spabs_v2.player.mapper.PlayerMapper;
import com.ahmadisyraf39.spabs_v2.player.repository.PlayerRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final PlayerMapper playerMapper;

    public PlayerResponse create(PlayerRequest request) {
        Player player = playerMapper.toEntity(request);
        return playerMapper.toResponse(playerRepository.save(player));
    }

    public PlayerResponse getById(Long id) {
        return playerMapper.toResponse(findEntityById(id));
    }

    public List<PlayerResponse> getAll() {
        return playerRepository.findAll().stream().map(playerMapper::toResponse).toList();
    }

    public PlayerResponse update(Long id, PlayerRequest request) {
        Player player = findEntityById(id);
        playerMapper.updateEntity(player, request);
        return playerMapper.toResponse(playerRepository.save(player));
    }

    public void delete(Long id) {
        playerRepository.delete(findEntityById(id));
    }

    private Player findEntityById(Long id) {
        return playerRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found: " + id));
    }
}
