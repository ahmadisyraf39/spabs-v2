package com.ahmadisyraf39.spabs_v2.sponsorship.controller;

import com.ahmadisyraf39.spabs_v2.sponsorship.dto.request.SponsorRequest;
import com.ahmadisyraf39.spabs_v2.sponsorship.dto.response.SponsorResponse;
import com.ahmadisyraf39.spabs_v2.sponsorship.service.SponsorService;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sponsors")
@RequiredArgsConstructor
public class SponsorController {

    private final SponsorService sponsorService;

    @PostMapping
    public ResponseEntity<SponsorResponse> create(@Valid @RequestBody SponsorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sponsorService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SponsorResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(sponsorService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<SponsorResponse>> getAll() {
        return ResponseEntity.ok(sponsorService.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<SponsorResponse> update(@PathVariable Long id, @Valid @RequestBody SponsorRequest request) {
        return ResponseEntity.ok(sponsorService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        sponsorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
