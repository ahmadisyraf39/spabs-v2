package com.ahmadisyraf39.spabs_v2.inventory.mapper;

import com.ahmadisyraf39.spabs_v2.inventory.dto.request.InventoryRequest;
import com.ahmadisyraf39.spabs_v2.inventory.dto.response.InventoryResponse;
import com.ahmadisyraf39.spabs_v2.inventory.entity.Inventory;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Inventory toEntity(InventoryRequest request);

    @InheritConfiguration(name = "toEntity")
    void updateEntity(@MappingTarget Inventory inventory, InventoryRequest request);

    InventoryResponse toResponse(Inventory inventory);
}
