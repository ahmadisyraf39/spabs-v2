package com.ahmadisyraf39.spabs_v2.finance.mapper;

import com.ahmadisyraf39.spabs_v2.finance.dto.request.FinanceTransactionRequest;
import com.ahmadisyraf39.spabs_v2.finance.dto.response.FinanceTransactionResponse;
import com.ahmadisyraf39.spabs_v2.finance.entity.FinanceTransaction;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FinanceTransactionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "referenceId", ignore = true)
    @Mapping(target = "referenceType", ignore = true)
    FinanceTransaction toEntity(FinanceTransactionRequest request);

    @InheritConfiguration(name = "toEntity")
    void updateEntity(@MappingTarget FinanceTransaction financeTransaction, FinanceTransactionRequest request);

    FinanceTransactionResponse toResponse(FinanceTransaction financeTransaction);
}
