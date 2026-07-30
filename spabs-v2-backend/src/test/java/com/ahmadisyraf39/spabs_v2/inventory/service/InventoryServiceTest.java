package com.ahmadisyraf39.spabs_v2.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.ahmadisyraf39.spabs_v2.common.exception.ResourceNotFoundException;
import com.ahmadisyraf39.spabs_v2.inventory.dto.request.InventoryRequest;
import com.ahmadisyraf39.spabs_v2.inventory.dto.response.InventoryResponse;
import com.ahmadisyraf39.spabs_v2.inventory.entity.Inventory;
import com.ahmadisyraf39.spabs_v2.inventory.entity.enums.InventoryCategory;
import com.ahmadisyraf39.spabs_v2.inventory.mapper.InventoryMapper;
import com.ahmadisyraf39.spabs_v2.inventory.repository.InventoryRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryMapper inventoryMapper;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void create_savesAndReturnsMappedResponse() {
        Inventory entity = new Inventory();
        when(inventoryMapper.toEntity(org.mockito.ArgumentMatchers.any())).thenReturn(entity);
        when(inventoryRepository.save(entity)).thenReturn(entity);
        when(inventoryMapper.toResponse(entity)).thenReturn(InventoryResponse.builder().build());

        InventoryRequest request = InventoryRequest.builder()
                .name("Footballs")
                .category(InventoryCategory.BALL)
                .currentQuantity(0)
                .build();

        assertThat(inventoryService.create(request)).isNotNull();
    }

    @Test
    void getById_notFound_throwsResourceNotFoundException() {
        when(inventoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.getById(1L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_notFound_throwsResourceNotFoundException() {
        when(inventoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.delete(1L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
