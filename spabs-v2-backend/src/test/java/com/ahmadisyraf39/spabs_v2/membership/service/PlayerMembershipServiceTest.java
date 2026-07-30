package com.ahmadisyraf39.spabs_v2.membership.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.ahmadisyraf39.spabs_v2.common.exception.ResourceNotFoundException;
import com.ahmadisyraf39.spabs_v2.membership.dto.request.PlayerMembershipRequest;
import com.ahmadisyraf39.spabs_v2.membership.dto.response.PlayerMembershipResponse;
import com.ahmadisyraf39.spabs_v2.membership.entity.PlayerMembership;
import com.ahmadisyraf39.spabs_v2.membership.entity.enums.PlayerMembershipStatus;
import com.ahmadisyraf39.spabs_v2.membership.mapper.PlayerMembershipMapper;
import com.ahmadisyraf39.spabs_v2.membership.repository.PlayerMembershipRepository;
import com.ahmadisyraf39.spabs_v2.player.entity.Player;
import com.ahmadisyraf39.spabs_v2.player.repository.PlayerRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlayerMembershipServiceTest {

    @Mock
    private PlayerMembershipRepository playerMembershipRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private PlayerMembershipMapper playerMembershipMapper;

    @InjectMocks
    private PlayerMembershipService playerMembershipService;

    @Test
    void create_resolvesPlayerAndSaves() {
        Player player = Player.builder().id(5L).build();
        when(playerRepository.findById(5L)).thenReturn(Optional.of(player));
        PlayerMembership entity = new PlayerMembership();
        when(playerMembershipMapper.toEntity(org.mockito.ArgumentMatchers.any())).thenReturn(entity);
        when(playerMembershipRepository.save(entity)).thenReturn(entity);
        when(playerMembershipMapper.toResponse(entity)).thenReturn(PlayerMembershipResponse.builder().build());

        playerMembershipService.create(PlayerMembershipRequest.builder()
                .playerId(5L)
                .status(PlayerMembershipStatus.ACTIVE)
                .joinedAt(LocalDate.now())
                .build());

        assertThat(entity.getPlayer()).isEqualTo(player);
    }

    @Test
    void create_playerNotFound_throwsResourceNotFoundException() {
        when(playerRepository.findById(5L)).thenReturn(Optional.empty());

        PlayerMembershipRequest request = PlayerMembershipRequest.builder()
                .playerId(5L)
                .status(PlayerMembershipStatus.ACTIVE)
                .joinedAt(LocalDate.now())
                .build();

        assertThatThrownBy(() -> playerMembershipService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getById_notFound_throwsResourceNotFoundException() {
        when(playerMembershipRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> playerMembershipService.getById(1L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
