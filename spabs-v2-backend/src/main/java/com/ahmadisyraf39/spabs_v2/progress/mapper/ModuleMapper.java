package com.ahmadisyraf39.spabs_v2.progress.mapper;

import com.ahmadisyraf39.spabs_v2.progress.dto.request.ModuleRequest;
import com.ahmadisyraf39.spabs_v2.progress.dto.response.ModuleResponse;
import com.ahmadisyraf39.spabs_v2.progress.entity.Module;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ModuleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "skill", ignore = true)
    Module toEntity(ModuleRequest request);

    @InheritConfiguration(name = "toEntity")
    void updateEntity(@MappingTarget Module module, ModuleRequest request);

    @Mapping(source = "skill.id", target = "skillId")
    ModuleResponse toResponse(Module module);
}
