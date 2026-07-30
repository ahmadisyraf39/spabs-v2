package com.ahmadisyraf39.spabs_v2.inventory.service;

import com.ahmadisyraf39.spabs_v2.common.exception.ResourceNotFoundException;
import com.ahmadisyraf39.spabs_v2.inventory.dto.request.InventoryRequest;
import com.ahmadisyraf39.spabs_v2.inventory.dto.response.InventoryResponse;
import com.ahmadisyraf39.spabs_v2.inventory.entity.Inventory;
import com.ahmadisyraf39.spabs_v2.inventory.mapper.InventoryMapper;
import com.ahmadisyraf39.spabs_v2.inventory.repository.InventoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryMapper inventoryMapper;

    public InventoryResponse create(InventoryRequest request) {
        Inventory inventory = inventoryMapper.toEntity(request);
        return inventoryMapper.toResponse(inventoryRepository.save(inventory));
    }

    public InventoryResponse getById(Long id) {
        return inventoryMapper.toResponse(findEntityById(id));
    }

    public List<InventoryResponse> getAll() {
        return inventoryRepository.findAll().stream().map(inventoryMapper::toResponse).toList();
    }

    public InventoryResponse update(Long id, InventoryRequest request) {
        Inventory inventory = findEntityById(id);
        inventoryMapper.updateEntity(inventory, request);
        return inventoryMapper.toResponse(inventoryRepository.save(inventory));
    }

    public void delete(Long id) {
        inventoryRepository.delete(findEntityById(id));
    }

    private Inventory findEntityById(Long id) {
        return inventoryRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found: " + id));
    }
}
