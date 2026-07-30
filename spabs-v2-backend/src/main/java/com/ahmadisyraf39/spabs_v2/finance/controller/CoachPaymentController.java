package com.ahmadisyraf39.spabs_v2.finance.controller;

import com.ahmadisyraf39.spabs_v2.finance.dto.request.CoachPaymentRequest;
import com.ahmadisyraf39.spabs_v2.finance.dto.response.CoachPaymentResponse;
import com.ahmadisyraf39.spabs_v2.finance.service.CoachPaymentService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/coach-payments")
@RequiredArgsConstructor
public class CoachPaymentController {

    private final CoachPaymentService coachPaymentService;

    @PostMapping
    public ResponseEntity<CoachPaymentResponse> create(@Valid @RequestBody CoachPaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(coachPaymentService.create(request));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<CoachPaymentResponse> pay(@PathVariable Long id) {
        return ResponseEntity.ok(coachPaymentService.pay(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CoachPaymentResponse> getById(
            @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(coachPaymentService.getById(id, principal));
    }

    @GetMapping
    public ResponseEntity<List<CoachPaymentResponse>> getAll(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(coachPaymentService.getAll(principal));
    }

    @GetMapping(params = "coachId")
    public ResponseEntity<List<CoachPaymentResponse>> getByCoach(
            @RequestParam Long coachId, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(coachPaymentService.getByCoach(coachId, principal));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CoachPaymentResponse> update(
            @PathVariable Long id, @Valid @RequestBody CoachPaymentRequest request) {
        return ResponseEntity.ok(coachPaymentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        coachPaymentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
