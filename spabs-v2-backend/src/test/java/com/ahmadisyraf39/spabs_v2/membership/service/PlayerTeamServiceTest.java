package com.ahmadisyraf39.spabs_v2.membership.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ahmadisyraf39.spabs_v2.common.exception.DuplicateResourceException;
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
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlayerTeamServiceTest {

    @Mock
    private PlayerTeamRepository playerTeamRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private PlayerTeamMapper playerTeamMapper;

    @InjectMocks
    private PlayerTeamService playerTeamService;

    private void stubHappyPath() {
        when(playerRepository.findById(5L)).thenReturn(Optional.of(Player.builder().id(5L).build()));
        when(teamRepository.findById(10L)).thenReturn(Optional.of(Team.builder().id(10L).build()));
        PlayerTeam entity = new PlayerTeam();
        when(playerTeamMapper.toEntity(any())).thenReturn(entity);
        when(playerTeamRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(playerTeamMapper.toResponse(any())).thenReturn(PlayerTeamResponse.builder().build());
    }

    @Test
    void create_jerseyNumberAlreadyActiveOnTeam_throwsDuplicateResourceException() {
        when(playerTeamRepository.findByTeamIdAndJerseyNumberAndStatus(10L, 7, PlayerTeamStatus.ACTIVE))
                .thenReturn(Optional.of(PlayerTeam.builder().id(99L).build()));

        PlayerTeamRequest request = PlayerTeamRequest.builder()
                .playerId(5L)
                .teamId(10L)
                .jerseyNumber(7)
                .status(PlayerTeamStatus.ACTIVE)
                .joinedAt(LocalDate.now())
                .build();

        assertThatThrownBy(() -> playerTeamService.create(request)).isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void create_noJerseyNumber_skipsDuplicateCheck() {
        stubHappyPath();

        PlayerTeamRequest request = PlayerTeamRequest.builder()
                .playerId(5L)
                .teamId(10L)
                .status(PlayerTeamStatus.ACTIVE)
                .joinedAt(LocalDate.now())
                .build();

        playerTeamService.create(request);

        verify(playerTeamRepository, never()).findByTeamIdAndJerseyNumberAndStatus(any(), any(), any());
    }

    @Test
    void create_inactiveStatus_skipsDuplicateCheckEvenWithJerseyNumber() {
        stubHappyPath();

        PlayerTeamRequest request = PlayerTeamRequest.builder()
                .playerId(5L)
                .teamId(10L)
                .jerseyNumber(7)
                .status(PlayerTeamStatus.INACTIVE)
                .joinedAt(LocalDate.now())
                .build();

        playerTeamService.create(request);

        verify(playerTeamRepository, never()).findByTeamIdAndJerseyNumberAndStatus(any(), any(), any());
    }

    @Test
    void update_excludesOwnRecordFromDuplicateCheck() {
        // The existing record with jersey 7 IS this player_team row itself (id=50) — updating
        // it (e.g. changing joinedAt) must not conflict with itself.
        when(playerTeamRepository.findByTeamIdAndJerseyNumberAndStatus(10L, 7, PlayerTeamStatus.ACTIVE))
                .thenReturn(Optional.of(PlayerTeam.builder().id(50L).build()));
        when(playerTeamRepository.findById(50L)).thenReturn(Optional.of(new PlayerTeam()));
        when(playerRepository.findById(5L)).thenReturn(Optional.of(Player.builder().id(5L).build()));
        when(teamRepository.findById(10L)).thenReturn(Optional.of(Team.builder().id(10L).build()));
        when(playerTeamRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(playerTeamMapper.toResponse(any())).thenReturn(PlayerTeamResponse.builder().build());

        PlayerTeamRequest request = PlayerTeamRequest.builder()
                .playerId(5L)
                .teamId(10L)
                .jerseyNumber(7)
                .status(PlayerTeamStatus.ACTIVE)
                .joinedAt(LocalDate.now())
                .build();

        assertThat(playerTeamService.update(50L, request)).isNotNull();
    }

    @Test
    void update_jerseyNumberTakenByAnotherRecord_throwsDuplicateResourceException() {
        when(playerTeamRepository.findByTeamIdAndJerseyNumberAndStatus(10L, 7, PlayerTeamStatus.ACTIVE))
                .thenReturn(Optional.of(PlayerTeam.builder().id(99L).build())); // a different record

        PlayerTeamRequest request = PlayerTeamRequest.builder()
                .playerId(5L)
                .teamId(10L)
                .jerseyNumber(7)
                .status(PlayerTeamStatus.ACTIVE)
                .joinedAt(LocalDate.now())
                .build();

        assertThatThrownBy(() -> playerTeamService.update(50L, request))
                .isInstanceOf(DuplicateResourceException.class);
    }
}
