package com.ahmadisyraf39.spabs_v2.attendance.controller;

import com.ahmadisyraf39.spabs_v2.attendance.dto.request.AttendanceRequest;
import com.ahmadisyraf39.spabs_v2.attendance.dto.request.BulkAttendanceRequest;
import com.ahmadisyraf39.spabs_v2.attendance.dto.response.AttendanceResponse;
import com.ahmadisyraf39.spabs_v2.attendance.dto.response.AttendanceSummaryResponse;
import com.ahmadisyraf39.spabs_v2.attendance.service.AttendanceService;
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
@RequestMapping("/api/v1/attendances")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping
    public ResponseEntity<AttendanceResponse> create(
            @Valid @RequestBody AttendanceRequest request, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(attendanceService.create(request, principal));
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<AttendanceResponse>> saveBulk(
            @Valid @RequestBody BulkAttendanceRequest request, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(attendanceService.saveBulk(request, principal));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AttendanceResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(attendanceService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<AttendanceResponse>> getAll() {
        return ResponseEntity.ok(attendanceService.getAll());
    }

    @GetMapping(params = "activityId")
    public ResponseEntity<List<AttendanceResponse>> getByActivity(@RequestParam Long activityId) {
        return ResponseEntity.ok(attendanceService.getByActivity(activityId));
    }

    @GetMapping(params = "playerId")
    public ResponseEntity<List<AttendanceResponse>> getByPlayer(@RequestParam Long playerId) {
        return ResponseEntity.ok(attendanceService.getByPlayer(playerId));
    }

    @GetMapping("/summary")
    public ResponseEntity<AttendanceSummaryResponse> getSummary(
            @RequestParam Long playerId, @RequestParam Long teamId) {
        return ResponseEntity.ok(attendanceService.getSummary(playerId, teamId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AttendanceResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody AttendanceRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(attendanceService.update(id, request, principal));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        attendanceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
