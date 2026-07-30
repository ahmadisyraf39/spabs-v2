package com.ahmadisyraf39.spabs_v2.user.controller;

import com.ahmadisyraf39.spabs_v2.security.UserPrincipal;
import com.ahmadisyraf39.spabs_v2.user.dto.request.ParentRequest;
import com.ahmadisyraf39.spabs_v2.user.dto.request.ParentSelfUpdateRequest;
import com.ahmadisyraf39.spabs_v2.user.dto.response.ParentResponse;
import com.ahmadisyraf39.spabs_v2.user.service.ParentService;
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
@RequestMapping("/api/v1/parents")
@RequiredArgsConstructor
public class ParentController {

    private final ParentService parentService;

    @PostMapping
    public ResponseEntity<ParentResponse> create(@Valid @RequestBody ParentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(parentService.create(request));
    }

    @GetMapping("/me")
    public ResponseEntity<ParentResponse> getMyProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(parentService.getMyProfile(principal));
    }

    @PutMapping("/me")
    public ResponseEntity<ParentResponse> updateMyProfile(
            @AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody ParentSelfUpdateRequest request) {
        return ResponseEntity.ok(parentService.updateMyProfile(principal, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(parentService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<ParentResponse>> getAll() {
        return ResponseEntity.ok(parentService.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ParentResponse> update(@PathVariable Long id, @Valid @RequestBody ParentRequest request) {
        return ResponseEntity.ok(parentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        parentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
