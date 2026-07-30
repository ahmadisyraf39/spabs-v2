package com.ahmadisyraf39.spabs_v2.membership.controller;

import com.ahmadisyraf39.spabs_v2.membership.dto.request.PlayerMembershipRequest;
import com.ahmadisyraf39.spabs_v2.membership.dto.response.PlayerMembershipResponse;
import com.ahmadisyraf39.spabs_v2.membership.service.PlayerMembershipService;
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
@RequestMapping("/api/v1/player-memberships")
@RequiredArgsConstructor
public class PlayerMembershipController {

    private final PlayerMembershipService playerMembershipService;

    @PostMapping
    public ResponseEntity<PlayerMembershipResponse> create(@Valid @RequestBody PlayerMembershipRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(playerMembershipService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlayerMembershipResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(playerMembershipService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<PlayerMembershipResponse>> getAll() {
        return ResponseEntity.ok(playerMembershipService.getAll());
    }

    @GetMapping(params = "playerId")
    public ResponseEntity<List<PlayerMembershipResponse>> getByPlayer(@RequestParam Long playerId) {
        return ResponseEntity.ok(playerMembershipService.getByPlayer(playerId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlayerMembershipResponse> update(
            @PathVariable Long id, @Valid @RequestBody PlayerMembershipRequest request) {
        return ResponseEntity.ok(playerMembershipService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        playerMembershipService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
