package com.ahmadisyraf39.spabs_v2.activity.mapper;

import com.ahmadisyraf39.spabs_v2.activity.dto.request.ActivityRequest;
import com.ahmadisyraf39.spabs_v2.activity.dto.response.ActivityResponse;
import com.ahmadisyraf39.spabs_v2.activity.entity.Activity;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ActivityMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "team", ignore = true)
    Activity toEntity(ActivityRequest request);

    @InheritConfiguration(name = "toEntity")
    void updateEntity(@MappingTarget Activity activity, ActivityRequest request);

    @Mapping(source = "team.id", target = "teamId")
    ActivityResponse toResponse(Activity activity);
}
