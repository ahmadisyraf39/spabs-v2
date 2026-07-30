package com.ahmadisyraf39.spabs_v2.progress.mapper;

import com.ahmadisyraf39.spabs_v2.progress.dto.response.PlayerModuleProgressResponse;
import com.ahmadisyraf39.spabs_v2.progress.entity.PlayerModuleProgress;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PlayerModuleProgressMapper {

    @Mapping(source = "module.id", target = "moduleId")
    @Mapping(source = "player.id", target = "playerId")
    @Mapping(source = "recordedByCoach.id", target = "recordedByCoachId")
    PlayerModuleProgressResponse toResponse(PlayerModuleProgress progress);
}
