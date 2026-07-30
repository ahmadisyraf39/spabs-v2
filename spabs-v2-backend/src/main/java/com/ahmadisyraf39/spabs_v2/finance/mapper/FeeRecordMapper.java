package com.ahmadisyraf39.spabs_v2.finance.mapper;

import com.ahmadisyraf39.spabs_v2.finance.dto.request.FeeRecordRequest;
import com.ahmadisyraf39.spabs_v2.finance.dto.response.FeeRecordResponse;
import com.ahmadisyraf39.spabs_v2.finance.entity.FeeRecord;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FeeRecordMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "player", ignore = true)
    @Mapping(target = "feeItem", ignore = true)
    @Mapping(target = "team", ignore = true)
    @Mapping(target = "amount", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "paymentMethod", ignore = true)
    @Mapping(target = "paidAt", ignore = true)
    FeeRecord toEntity(FeeRecordRequest request);

    @InheritConfiguration(name = "toEntity")
    void updateEntity(@MappingTarget FeeRecord feeRecord, FeeRecordRequest request);

    @Mapping(source = "player.id", target = "playerId")
    @Mapping(source = "feeItem.id", target = "feeItemId")
    @Mapping(source = "feeItem.name", target = "feeItemName")
    @Mapping(source = "team.id", target = "teamId")
    @Mapping(target = "overdue", ignore = true)
    FeeRecordResponse toResponse(FeeRecord feeRecord);
}
