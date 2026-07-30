package com.ahmadisyraf39.spabs_v2.user.mapper;

import com.ahmadisyraf39.spabs_v2.user.dto.request.ParentRequest;
import com.ahmadisyraf39.spabs_v2.user.dto.response.ParentResponse;
import com.ahmadisyraf39.spabs_v2.user.entity.Parent;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ParentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    Parent toEntity(ParentRequest request);

    @InheritConfiguration(name = "toEntity")
    void updateEntity(@MappingTarget Parent parent, ParentRequest request);

    @Mapping(source = "user.id", target = "userId")
    ParentResponse toResponse(Parent parent);
}
