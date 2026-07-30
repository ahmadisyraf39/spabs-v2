package com.ahmadisyraf39.spabs_v2.user.mapper;

import com.ahmadisyraf39.spabs_v2.user.dto.request.CoachRequest;
import com.ahmadisyraf39.spabs_v2.user.dto.response.CoachResponse;
import com.ahmadisyraf39.spabs_v2.user.entity.Coach;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CoachMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    Coach toEntity(CoachRequest request);

    @InheritConfiguration(name = "toEntity")
    void updateEntity(@MappingTarget Coach coach, CoachRequest request);

    @Mapping(source = "user.id", target = "userId")
    CoachResponse toResponse(Coach coach);
}
