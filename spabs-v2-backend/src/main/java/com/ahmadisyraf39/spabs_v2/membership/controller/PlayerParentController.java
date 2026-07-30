package com.ahmadisyraf39.spabs_v2.membership.controller;

import com.ahmadisyraf39.spabs_v2.membership.dto.request.PlayerParentRequest;
import com.ahmadisyraf39.spabs_v2.membership.dto.response.PlayerParentResponse;
import com.ahmadisyraf39.spabs_v2.membership.service.PlayerParentService;
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
@RequestMapping("/api/v1/player-parents")
@RequiredArgsConstructor
public class PlayerParentController {

    private final PlayerParentService playerParentService;

    @PostMapping
    public ResponseEntity<PlayerParentResponse> create(@Valid @RequestBody PlayerParentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(playerParentService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlayerParentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(playerParentService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<PlayerParentResponse>> getAll() {
        return ResponseEntity.ok(playerParentService.getAll());
    }

    @GetMapping(params = "playerId")
    public ResponseEntity<List<PlayerParentResponse>> getByPlayer(@RequestParam Long playerId) {
        return ResponseEntity.ok(playerParentService.getByPlayer(playerId));
    }

    @GetMapping(params = "parentId")
    public ResponseEntity<List<PlayerParentResponse>> getByParent(@RequestParam Long parentId) {
        return ResponseEntity.ok(playerParentService.getByParent(parentId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlayerParentResponse> update(
            @PathVariable Long id, @Valid @RequestBody PlayerParentRequest request) {
        return ResponseEntity.ok(playerParentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        playerParentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
