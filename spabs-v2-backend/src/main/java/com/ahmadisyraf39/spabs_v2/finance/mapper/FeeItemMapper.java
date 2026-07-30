package com.ahmadisyraf39.spabs_v2.finance.mapper;

import com.ahmadisyraf39.spabs_v2.finance.dto.request.FeeItemRequest;
import com.ahmadisyraf39.spabs_v2.finance.dto.response.FeeItemResponse;
import com.ahmadisyraf39.spabs_v2.finance.entity.FeeItem;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FeeItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    FeeItem toEntity(FeeItemRequest request);

    @InheritConfiguration(name = "toEntity")
    void updateEntity(@MappingTarget FeeItem feeItem, FeeItemRequest request);

    FeeItemResponse toResponse(FeeItem feeItem);
}
