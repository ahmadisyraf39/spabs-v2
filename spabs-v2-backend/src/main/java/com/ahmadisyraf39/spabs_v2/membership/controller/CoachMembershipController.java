package com.ahmadisyraf39.spabs_v2.membership.controller;

import com.ahmadisyraf39.spabs_v2.membership.dto.request.CoachMembershipRequest;
import com.ahmadisyraf39.spabs_v2.membership.dto.response.CoachMembershipResponse;
import com.ahmadisyraf39.spabs_v2.membership.service.CoachMembershipService;
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
@RequestMapping("/api/v1/coach-memberships")
@RequiredArgsConstructor
public class CoachMembershipController {

    private final CoachMembershipService coachMembershipService;

    @PostMapping
    public ResponseEntity<CoachMembershipResponse> create(@Valid @RequestBody CoachMembershipRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(coachMembershipService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CoachMembershipResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(coachMembershipService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<CoachMembershipResponse>> getAll() {
        return ResponseEntity.ok(coachMembershipService.getAll());
    }

    @GetMapping(params = "coachId")
    public ResponseEntity<List<CoachMembershipResponse>> getByCoach(@RequestParam Long coachId) {
        return ResponseEntity.ok(coachMembershipService.getByCoach(coachId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CoachMembershipResponse> update(
            @PathVariable Long id, @Valid @RequestBody CoachMembershipRequest request) {
        return ResponseEntity.ok(coachMembershipService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        coachMembershipService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
