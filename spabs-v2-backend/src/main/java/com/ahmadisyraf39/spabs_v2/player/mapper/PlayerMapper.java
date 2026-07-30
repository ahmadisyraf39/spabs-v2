package com.ahmadisyraf39.spabs_v2.player.mapper;

import com.ahmadisyraf39.spabs_v2.player.dto.request.PlayerRequest;
import com.ahmadisyraf39.spabs_v2.player.dto.response.PlayerResponse;
import com.ahmadisyraf39.spabs_v2.player.entity.Player;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PlayerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Player toEntity(PlayerRequest request);

    @InheritConfiguration(name = "toEntity")
    void updateEntity(@MappingTarget Player player, PlayerRequest request);

    PlayerResponse toResponse(Player player);
}
