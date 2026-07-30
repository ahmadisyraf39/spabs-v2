package com.ahmadisyraf39.spabs_v2.membership.mapper;

import com.ahmadisyraf39.spabs_v2.membership.dto.request.PlayerParentRequest;
import com.ahmadisyraf39.spabs_v2.membership.dto.response.PlayerParentResponse;
import com.ahmadisyraf39.spabs_v2.membership.entity.PlayerParent;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PlayerParentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "player", ignore = true)
    @Mapping(target = "parent", ignore = true)
    PlayerParent toEntity(PlayerParentRequest request);

    @InheritConfiguration(name = "toEntity")
    void updateEntity(@MappingTarget PlayerParent playerParent, PlayerParentRequest request);

    @Mapping(source = "player.id", target = "playerId")
    @Mapping(source = "parent.id", target = "parentId")
    PlayerParentResponse toResponse(PlayerParent playerParent);
}
