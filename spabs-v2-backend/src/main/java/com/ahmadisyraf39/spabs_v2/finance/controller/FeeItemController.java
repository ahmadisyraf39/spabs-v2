package com.ahmadisyraf39.spabs_v2.finance.controller;

import com.ahmadisyraf39.spabs_v2.finance.dto.request.FeeItemRequest;
import com.ahmadisyraf39.spabs_v2.finance.dto.response.FeeItemResponse;
import com.ahmadisyraf39.spabs_v2.finance.service.FeeItemService;
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
@RequestMapping("/api/v1/fee-items")
@RequiredArgsConstructor
public class FeeItemController {

    private final FeeItemService feeItemService;

    @PostMapping
    public ResponseEntity<FeeItemResponse> create(@Valid @RequestBody FeeItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(feeItemService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FeeItemResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(feeItemService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<FeeItemResponse>> getAll() {
        return ResponseEntity.ok(feeItemService.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<FeeItemResponse> update(@PathVariable Long id, @Valid @RequestBody FeeItemRequest request) {
        return ResponseEntity.ok(feeItemService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        feeItemService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
