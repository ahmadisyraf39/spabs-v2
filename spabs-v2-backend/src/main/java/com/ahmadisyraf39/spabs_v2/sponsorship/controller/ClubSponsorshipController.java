package com.ahmadisyraf39.spabs_v2.sponsorship.controller;

import com.ahmadisyraf39.spabs_v2.sponsorship.dto.request.ClubSponsorshipRequest;
import com.ahmadisyraf39.spabs_v2.sponsorship.dto.response.ClubSponsorshipResponse;
import com.ahmadisyraf39.spabs_v2.sponsorship.service.ClubSponsorshipService;
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
@RequestMapping("/api/v1/club-sponsorships")
@RequiredArgsConstructor
public class ClubSponsorshipController {

    private final ClubSponsorshipService clubSponsorshipService;

    @PostMapping
    public ResponseEntity<ClubSponsorshipResponse> create(@Valid @RequestBody ClubSponsorshipRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clubSponsorshipService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClubSponsorshipResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(clubSponsorshipService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<ClubSponsorshipResponse>> getAll() {
        return ResponseEntity.ok(clubSponsorshipService.getAll());
    }

    @GetMapping(params = "sponsorId")
    public ResponseEntity<List<ClubSponsorshipResponse>> getBySponsor(@RequestParam Long sponsorId) {
        return ResponseEntity.ok(clubSponsorshipService.getBySponsor(sponsorId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClubSponsorshipResponse> update(
            @PathVariable Long id, @Valid @RequestBody ClubSponsorshipRequest request) {
        return ResponseEntity.ok(clubSponsorshipService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        clubSponsorshipService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
