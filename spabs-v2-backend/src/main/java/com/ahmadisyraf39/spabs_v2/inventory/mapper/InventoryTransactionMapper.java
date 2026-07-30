package com.ahmadisyraf39.spabs_v2.inventory.mapper;

import com.ahmadisyraf39.spabs_v2.inventory.dto.request.InventoryTransactionRequest;
import com.ahmadisyraf39.spabs_v2.inventory.dto.response.InventoryTransactionResponse;
import com.ahmadisyraf39.spabs_v2.inventory.entity.InventoryTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InventoryTransactionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "inventory", ignore = true)
    InventoryTransaction toEntity(InventoryTransactionRequest request);

    @Mapping(source = "inventory.id", target = "inventoryId")
    @Mapping(source = "inventory.name", target = "inventoryName")
    InventoryTransactionResponse toResponse(InventoryTransaction transaction);
}
