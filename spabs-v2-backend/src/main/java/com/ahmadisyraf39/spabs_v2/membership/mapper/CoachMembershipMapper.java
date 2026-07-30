package com.ahmadisyraf39.spabs_v2.membership.mapper;

import com.ahmadisyraf39.spabs_v2.membership.dto.request.CoachMembershipRequest;
import com.ahmadisyraf39.spabs_v2.membership.dto.response.CoachMembershipResponse;
import com.ahmadisyraf39.spabs_v2.membership.entity.CoachMembership;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CoachMembershipMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "coach", ignore = true)
    CoachMembership toEntity(CoachMembershipRequest request);

    @InheritConfiguration(name = "toEntity")
    void updateEntity(@MappingTarget CoachMembership coachMembership, CoachMembershipRequest request);

    @Mapping(source = "coach.id", target = "coachId")
    CoachMembershipResponse toResponse(CoachMembership coachMembership);
}
