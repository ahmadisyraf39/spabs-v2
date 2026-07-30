package com.ahmadisyraf39.spabs_v2.finance.controller;

import com.ahmadisyraf39.spabs_v2.finance.dto.request.FeeRecordRequest;
import com.ahmadisyraf39.spabs_v2.finance.dto.response.FeeRecordResponse;
import com.ahmadisyraf39.spabs_v2.finance.dto.response.FeeSummaryResponse;
import com.ahmadisyraf39.spabs_v2.finance.service.FeeRecordService;
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
@RequestMapping("/api/v1/fee-records")
@RequiredArgsConstructor
public class FeeRecordController {

    private final FeeRecordService feeRecordService;

    @PostMapping
    public ResponseEntity<FeeRecordResponse> assignFee(
            @Valid @RequestBody FeeRecordRequest request, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(feeRecordService.assignFee(request, principal));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<FeeRecordResponse> pay(
            @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(feeRecordService.pay(id, principal));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FeeRecordResponse> getById(
            @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(feeRecordService.getById(id, principal));
    }

    @GetMapping(params = "playerId")
    public ResponseEntity<List<FeeRecordResponse>> getByPlayer(
            @RequestParam Long playerId, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(feeRecordService.getByPlayer(playerId, principal));
    }

    @GetMapping(params = "teamId")
    public ResponseEntity<List<FeeRecordResponse>> getByTeam(
            @RequestParam Long teamId, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(feeRecordService.getByTeam(teamId, principal));
    }

    @GetMapping("/team/{teamId}/summary")
    public ResponseEntity<FeeSummaryResponse> getSummaryByTeam(
            @PathVariable Long teamId, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(feeRecordService.getSummaryByTeam(teamId, principal));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FeeRecordResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody FeeRecordRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(feeRecordService.update(id, request, principal));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        feeRecordService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
