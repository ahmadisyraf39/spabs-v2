package com.ahmadisyraf39.spabs_v2.membership.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.ahmadisyraf39.spabs_v2.common.exception.ResourceNotFoundException;
import com.ahmadisyraf39.spabs_v2.membership.dto.request.PlayerParentRequest;
import com.ahmadisyraf39.spabs_v2.membership.dto.response.PlayerParentResponse;
import com.ahmadisyraf39.spabs_v2.membership.entity.PlayerParent;
import com.ahmadisyraf39.spabs_v2.membership.entity.enums.ParentRelationship;
import com.ahmadisyraf39.spabs_v2.membership.mapper.PlayerParentMapper;
import com.ahmadisyraf39.spabs_v2.membership.repository.PlayerParentRepository;
import com.ahmadisyraf39.spabs_v2.player.entity.Player;
import com.ahmadisyraf39.spabs_v2.player.repository.PlayerRepository;
import com.ahmadisyraf39.spabs_v2.user.entity.Parent;
import com.ahmadisyraf39.spabs_v2.user.repository.ParentRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlayerParentServiceTest {

    @Mock
    private PlayerParentRepository playerParentRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private ParentRepository parentRepository;

    @Mock
    private PlayerParentMapper playerParentMapper;

    @InjectMocks
    private PlayerParentService playerParentService;

    @Test
    void create_resolvesPlayerAndParentAndSaves() {
        Player player = Player.builder().id(5L).build();
        Parent parent = Parent.builder().id(30L).build();
        when(playerRepository.findById(5L)).thenReturn(Optional.of(player));
        when(parentRepository.findById(30L)).thenReturn(Optional.of(parent));
        PlayerParent entity = new PlayerParent();
        when(playerParentMapper.toEntity(org.mockito.ArgumentMatchers.any())).thenReturn(entity);
        when(playerParentRepository.save(entity)).thenReturn(entity);
        when(playerParentMapper.toResponse(entity)).thenReturn(PlayerParentResponse.builder().build());

        playerParentService.create(PlayerParentRequest.builder()
                .playerId(5L)
                .parentId(30L)
                .relationship(ParentRelationship.FATHER)
                .build());

        assertThat(entity.getPlayer()).isEqualTo(player);
        assertThat(entity.getParent()).isEqualTo(parent);
    }

    @Test
    void create_parentNotFound_throwsResourceNotFoundException() {
        when(playerParentMapper.toEntity(org.mockito.ArgumentMatchers.any())).thenReturn(new PlayerParent());
        when(playerRepository.findById(5L)).thenReturn(Optional.of(Player.builder().id(5L).build()));
        when(parentRepository.findById(30L)).thenReturn(Optional.empty());

        PlayerParentRequest request = PlayerParentRequest.builder()
                .playerId(5L)
                .parentId(30L)
                .relationship(ParentRelationship.FATHER)
                .build();

        assertThatThrownBy(() -> playerParentService.create(request)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getById_notFound_throwsResourceNotFoundException() {
        when(playerParentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> playerParentService.getById(1L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
