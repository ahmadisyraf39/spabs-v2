package com.ahmadisyraf39.spabs_v2.user.controller;

import com.ahmadisyraf39.spabs_v2.security.UserPrincipal;
import com.ahmadisyraf39.spabs_v2.user.dto.request.CoachRequest;
import com.ahmadisyraf39.spabs_v2.user.dto.response.CoachResponse;
import com.ahmadisyraf39.spabs_v2.user.service.CoachService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/coaches")
@RequiredArgsConstructor
public class CoachController {

    private final CoachService coachService;

    @PostMapping
    public ResponseEntity<CoachResponse> create(@Valid @RequestBody CoachRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(coachService.create(request));
    }

    @GetMapping("/me")
    public ResponseEntity<CoachResponse> getMyProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(coachService.getMyProfile(principal));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CoachResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(coachService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<CoachResponse>> getAll() {
        return ResponseEntity.ok(coachService.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CoachResponse> update(@PathVariable Long id, @Valid @RequestBody CoachRequest request) {
        return ResponseEntity.ok(coachService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        coachService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
