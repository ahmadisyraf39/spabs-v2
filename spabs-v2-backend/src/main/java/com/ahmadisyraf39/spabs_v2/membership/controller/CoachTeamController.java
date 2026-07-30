package com.ahmadisyraf39.spabs_v2.membership.controller;

import com.ahmadisyraf39.spabs_v2.membership.dto.request.CoachTeamRequest;
import com.ahmadisyraf39.spabs_v2.membership.dto.response.CoachTeamResponse;
import com.ahmadisyraf39.spabs_v2.membership.service.CoachTeamService;
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
@RequestMapping("/api/v1/coach-teams")
@RequiredArgsConstructor
public class CoachTeamController {

    private final CoachTeamService coachTeamService;

    @PostMapping
    public ResponseEntity<CoachTeamResponse> create(@Valid @RequestBody CoachTeamRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(coachTeamService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CoachTeamResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(coachTeamService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<CoachTeamResponse>> getAll() {
        return ResponseEntity.ok(coachTeamService.getAll());
    }

    @GetMapping(params = "coachId")
    public ResponseEntity<List<CoachTeamResponse>> getByCoach(@RequestParam Long coachId) {
        return ResponseEntity.ok(coachTeamService.getByCoach(coachId));
    }

    @GetMapping(params = "teamId")
    public ResponseEntity<List<CoachTeamResponse>> getByTeam(@RequestParam Long teamId) {
        return ResponseEntity.ok(coachTeamService.getByTeam(teamId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CoachTeamResponse> update(
            @PathVariable Long id, @Valid @RequestBody CoachTeamRequest request) {
        return ResponseEntity.ok(coachTeamService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        coachTeamService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
