package com.ahmadisyraf39.spabs_v2.inventory.service;

import com.ahmadisyraf39.spabs_v2.common.exception.ResourceNotFoundException;
import com.ahmadisyraf39.spabs_v2.finance.entity.enums.FinanceCategory;
import com.ahmadisyraf39.spabs_v2.finance.entity.enums.FinanceReferenceType;
import com.ahmadisyraf39.spabs_v2.finance.entity.enums.FinanceType;
import com.ahmadisyraf39.spabs_v2.finance.service.FinanceTransactionService;
import com.ahmadisyraf39.spabs_v2.inventory.dto.request.InventoryTransactionRequest;
import com.ahmadisyraf39.spabs_v2.inventory.dto.response.InventoryTransactionResponse;
import com.ahmadisyraf39.spabs_v2.inventory.entity.Inventory;
import com.ahmadisyraf39.spabs_v2.inventory.entity.InventoryTransaction;
import com.ahmadisyraf39.spabs_v2.inventory.entity.enums.InventoryTransactionType;
import com.ahmadisyraf39.spabs_v2.inventory.mapper.InventoryTransactionMapper;
import com.ahmadisyraf39.spabs_v2.inventory.repository.InventoryRepository;
import com.ahmadisyraf39.spabs_v2.inventory.repository.InventoryTransactionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryTransactionService {

    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionMapper inventoryTransactionMapper;
    private final FinanceTransactionService financeTransactionService;

    public InventoryTransactionResponse recordTransaction(InventoryTransactionRequest request) {
        Inventory inventory = findInventoryById(request.getInventoryId());

        switch (request.getTransactionType()) {
            case INITIAL_STOCK, PURCHASE ->
                    inventory.setCurrentQuantity(inventory.getCurrentQuantity() + request.getQuantity());
            case DAMAGE, LOST ->
                    inventory.setCurrentQuantity(Math.max(0, inventory.getCurrentQuantity() - request.getQuantity()));
            case ADJUSTMENT -> inventory.setCurrentQuantity(request.getQuantity());
        }
        inventoryRepository.save(inventory);

        InventoryTransaction transaction = inventoryTransactionMapper.toEntity(request);
        transaction.setInventory(inventory);
        InventoryTransaction saved = inventoryTransactionRepository.save(transaction);

        if (request.getTransactionType() == InventoryTransactionType.PURCHASE && request.getPrice() != null) {
            financeTransactionService.recordLinked(
                    FinanceType.EXPENSE,
                    FinanceCategory.INVENTORY_PURCHASE,
                    request.getPrice(),
                    inventory.getName() + " purchase",
                    request.getTransactionDate(),
                    saved.getId(),
                    FinanceReferenceType.INVENTORY);
        }

        return inventoryTransactionMapper.toResponse(saved);
    }

    public InventoryTransactionResponse getById(Long id) {
        return inventoryTransactionMapper.toResponse(findEntityById(id));
    }

    public List<InventoryTransactionResponse> getByInventory(Long inventoryId) {
        return inventoryTransactionRepository.findByInventoryId(inventoryId).stream()
                .map(inventoryTransactionMapper::toResponse)
                .toList();
    }

    public void delete(Long id) {
        inventoryTransactionRepository.delete(findEntityById(id));
    }

    private InventoryTransaction findEntityById(Long id) {
        return inventoryTransactionRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("InventoryTransaction not found: " + id));
    }

    private Inventory findInventoryById(Long inventoryId) {
        return inventoryRepository
                .findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found: " + inventoryId));
    }
}
