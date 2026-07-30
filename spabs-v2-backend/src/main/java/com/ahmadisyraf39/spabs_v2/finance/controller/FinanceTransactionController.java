package com.ahmadisyraf39.spabs_v2.finance.controller;

import com.ahmadisyraf39.spabs_v2.finance.dto.request.ActivityFinanceRequest;
import com.ahmadisyraf39.spabs_v2.finance.dto.request.FinanceTransactionRequest;
import com.ahmadisyraf39.spabs_v2.finance.dto.response.FinanceSummaryResponse;
import com.ahmadisyraf39.spabs_v2.finance.dto.response.FinanceTransactionResponse;
import com.ahmadisyraf39.spabs_v2.finance.service.FinanceTransactionService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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
@RequestMapping("/api/v1/finance-transactions")
@RequiredArgsConstructor
public class FinanceTransactionController {

    private final FinanceTransactionService financeTransactionService;

    @PostMapping
    public ResponseEntity<FinanceTransactionResponse> create(@Valid @RequestBody FinanceTransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(financeTransactionService.create(request));
    }

    @PostMapping("/activity-entry")
    public ResponseEntity<FinanceTransactionResponse> recordActivityFinanceEntry(
            @Valid @RequestBody ActivityFinanceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(financeTransactionService.recordActivityFinanceEntry(request));
    }

    @GetMapping("/summary")
    public ResponseEntity<FinanceSummaryResponse> getSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(financeTransactionService.getSummary(startDate, endDate));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FinanceTransactionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(financeTransactionService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<FinanceTransactionResponse>> getAll() {
        return ResponseEntity.ok(financeTransactionService.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<FinanceTransactionResponse> update(
            @PathVariable Long id, @Valid @RequestBody FinanceTransactionRequest request) {
        return ResponseEntity.ok(financeTransactionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        financeTransactionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
