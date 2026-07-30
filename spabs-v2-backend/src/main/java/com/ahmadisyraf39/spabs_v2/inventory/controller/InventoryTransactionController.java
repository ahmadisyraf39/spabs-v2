package com.ahmadisyraf39.spabs_v2.inventory.controller;

import com.ahmadisyraf39.spabs_v2.inventory.dto.request.InventoryTransactionRequest;
import com.ahmadisyraf39.spabs_v2.inventory.dto.response.InventoryTransactionResponse;
import com.ahmadisyraf39.spabs_v2.inventory.service.InventoryTransactionService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventory-transactions")
@RequiredArgsConstructor
public class InventoryTransactionController {

    private final InventoryTransactionService inventoryTransactionService;

    @PostMapping
    public ResponseEntity<InventoryTransactionResponse> recordTransaction(
            @Valid @RequestBody InventoryTransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryTransactionService.recordTransaction(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryTransactionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryTransactionService.getById(id));
    }

    @GetMapping(params = "inventoryId")
    public ResponseEntity<List<InventoryTransactionResponse>> getByInventory(@RequestParam Long inventoryId) {
        return ResponseEntity.ok(inventoryTransactionService.getByInventory(inventoryId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        inventoryTransactionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
