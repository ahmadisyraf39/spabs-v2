package com.ahmadisyraf39.spabs_v2.progress.mapper;

import com.ahmadisyraf39.spabs_v2.progress.dto.request.SkillRequest;
import com.ahmadisyraf39.spabs_v2.progress.dto.response.SkillResponse;
import com.ahmadisyraf39.spabs_v2.progress.entity.Skill;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SkillMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Skill toEntity(SkillRequest request);

    @InheritConfiguration(name = "toEntity")
    void updateEntity(@MappingTarget Skill skill, SkillRequest request);

    SkillResponse toResponse(Skill skill);
}
