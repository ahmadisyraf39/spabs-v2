package com.ahmadisyraf39.spabs_v2.progress.controller;

import com.ahmadisyraf39.spabs_v2.progress.dto.request.BulkPlayerModuleProgressRequest;
import com.ahmadisyraf39.spabs_v2.progress.dto.request.PlayerModuleProgressRequest;
import com.ahmadisyraf39.spabs_v2.progress.dto.response.PlayerModuleProgressResponse;
import com.ahmadisyraf39.spabs_v2.progress.dto.response.PlayerTeamProgressResponse;
import com.ahmadisyraf39.spabs_v2.progress.service.PlayerModuleProgressService;
import com.ahmadisyraf39.spabs_v2.security.UserPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/player-progress")
@RequiredArgsConstructor
public class PlayerModuleProgressController {

    private final PlayerModuleProgressService playerModuleProgressService;

    @PostMapping
    public ResponseEntity<PlayerModuleProgressResponse> save(
            @Valid @RequestBody PlayerModuleProgressRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(playerModuleProgressService.save(request, principal));
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<PlayerModuleProgressResponse>> saveBulk(
            @Valid @RequestBody BulkPlayerModuleProgressRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(playerModuleProgressService.saveBulk(request, principal));
    }

    @GetMapping("/summary")
    public ResponseEntity<PlayerTeamProgressResponse> getSummary(
            @RequestParam Long playerId,
            @RequestParam Long teamId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(playerModuleProgressService.getPlayerTeamProgress(playerId, teamId, principal));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        playerModuleProgressService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
