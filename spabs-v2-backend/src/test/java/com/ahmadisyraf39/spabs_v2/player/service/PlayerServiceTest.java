package com.ahmadisyraf39.spabs_v2.player.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.ahmadisyraf39.spabs_v2.common.exception.ResourceNotFoundException;
import com.ahmadisyraf39.spabs_v2.player.dto.request.PlayerRequest;
import com.ahmadisyraf39.spabs_v2.player.dto.response.PlayerResponse;
import com.ahmadisyraf39.spabs_v2.player.entity.Player;
import com.ahmadisyraf39.spabs_v2.player.entity.enums.Gender;
import com.ahmadisyraf39.spabs_v2.player.mapper.PlayerMapper;
import com.ahmadisyraf39.spabs_v2.player.repository.PlayerRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private PlayerMapper playerMapper;

    @InjectMocks
    private PlayerService playerService;

    @Test
    void create_savesAndReturnsMappedResponse() {
        Player entity = new Player();
        when(playerMapper.toEntity(org.mockito.ArgumentMatchers.any())).thenReturn(entity);
        when(playerRepository.save(entity)).thenReturn(entity);
        when(playerMapper.toResponse(entity)).thenReturn(PlayerResponse.builder().build());

        PlayerRequest request = PlayerRequest.builder()
                .fullName("Ali Hassan")
                .dateOfBirth(LocalDate.of(2014, 3, 12))
                .gender(Gender.MALE)
                .build();

        assertThat(playerService.create(request)).isNotNull();
    }

    @Test
    void getById_notFound_throwsResourceNotFoundException() {
        when(playerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> playerService.getById(1L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_notFound_throwsResourceNotFoundException() {
        when(playerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> playerService.delete(1L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
