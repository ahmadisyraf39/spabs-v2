package com.ahmadisyraf39.spabs_v2.activity.controller;

import com.ahmadisyraf39.spabs_v2.activity.dto.request.ActivityRequest;
import com.ahmadisyraf39.spabs_v2.activity.dto.request.RecurringActivityRequest;
import com.ahmadisyraf39.spabs_v2.activity.dto.response.ActivityResponse;
import com.ahmadisyraf39.spabs_v2.activity.service.ActivityService;
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
@RequestMapping("/api/v1/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @PostMapping
    public ResponseEntity<ActivityResponse> create(@Valid @RequestBody ActivityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(activityService.create(request));
    }

    @PostMapping("/recurring")
    public ResponseEntity<List<ActivityResponse>> createRecurring(
            @Valid @RequestBody RecurringActivityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(activityService.createRecurring(request));
    }

    @GetMapping("/my-upcoming")
    public ResponseEntity<List<ActivityResponse>> getMyUpcoming(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(activityService.getMyUpcoming(principal));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActivityResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(activityService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<ActivityResponse>> getAll() {
        return ResponseEntity.ok(activityService.getAll());
    }

    @GetMapping(params = "teamId")
    public ResponseEntity<List<ActivityResponse>> getByTeam(@RequestParam Long teamId) {
        return ResponseEntity.ok(activityService.getByTeam(teamId));
    }

    @GetMapping(params = "coachId")
    public ResponseEntity<List<ActivityResponse>> getUpcomingForCoach(
            @RequestParam Long coachId, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(activityService.getUpcomingForCoach(coachId, principal));
    }

    @GetMapping(params = "parentId")
    public ResponseEntity<List<ActivityResponse>> getUpcomingForParent(
            @RequestParam Long parentId, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(activityService.getUpcomingForParent(parentId, principal));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ActivityResponse> update(
            @PathVariable Long id, @Valid @RequestBody ActivityRequest request) {
        return ResponseEntity.ok(activityService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        activityService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
