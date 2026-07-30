package com.ahmadisyraf39.spabs_v2.finance.mapper;

import com.ahmadisyraf39.spabs_v2.finance.dto.request.CoachPaymentRequest;
import com.ahmadisyraf39.spabs_v2.finance.dto.response.CoachPaymentResponse;
import com.ahmadisyraf39.spabs_v2.finance.entity.CoachPayment;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CoachPaymentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "coach", ignore = true)
    @Mapping(target = "status", ignore = true)
    CoachPayment toEntity(CoachPaymentRequest request);

    @InheritConfiguration(name = "toEntity")
    void updateEntity(@MappingTarget CoachPayment coachPayment, CoachPaymentRequest request);

    @Mapping(source = "coach.id", target = "coachId")
    CoachPaymentResponse toResponse(CoachPayment coachPayment);
}
