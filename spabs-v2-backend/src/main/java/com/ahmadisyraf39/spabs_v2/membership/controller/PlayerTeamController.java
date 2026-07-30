package com.ahmadisyraf39.spabs_v2.membership.controller;

import com.ahmadisyraf39.spabs_v2.membership.dto.request.PlayerTeamRequest;
import com.ahmadisyraf39.spabs_v2.membership.dto.response.PlayerTeamResponse;
import com.ahmadisyraf39.spabs_v2.membership.service.PlayerTeamService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/player-teams")
@RequiredArgsConstructor
public class PlayerTeamController {

    private final PlayerTeamService playerTeamService;

    @PostMapping
    public ResponseEntity<PlayerTeamResponse> create(@Valid @RequestBody PlayerTeamRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(playerTeamService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlayerTeamResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(playerTeamService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<PlayerTeamResponse>> getAll() {
        return ResponseEntity.ok(playerTeamService.getAll());
    }

    @GetMapping(params = "playerId")
    public ResponseEntity<List<PlayerTeamResponse>> getByPlayer(@RequestParam Long playerId) {
        return ResponseEntity.ok(playerTeamService.getByPlayer(playerId));
    }

    @GetMapping(params = "teamId")
    public ResponseEntity<List<PlayerTeamResponse>> getByTeam(@RequestParam Long teamId) {
        return ResponseEntity.ok(playerTeamService.getByTeam(teamId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlayerTeamResponse> update(
            @PathVariable Long id, @Valid @RequestBody PlayerTeamRequest request) {
        return ResponseEntity.ok(playerTeamService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        playerTeamService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
