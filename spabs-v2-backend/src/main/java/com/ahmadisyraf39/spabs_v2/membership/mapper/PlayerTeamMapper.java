package com.ahmadisyraf39.spabs_v2.membership.mapper;

import com.ahmadisyraf39.spabs_v2.membership.dto.request.PlayerTeamRequest;
import com.ahmadisyraf39.spabs_v2.membership.dto.response.PlayerTeamResponse;
import com.ahmadisyraf39.spabs_v2.membership.entity.PlayerTeam;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PlayerTeamMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "player", ignore = true)
    @Mapping(target = "team", ignore = true)
    PlayerTeam toEntity(PlayerTeamRequest request);

    @InheritConfiguration(name = "toEntity")
    void updateEntity(@MappingTarget PlayerTeam playerTeam, PlayerTeamRequest request);

    @Mapping(source = "player.id", target = "playerId")
    @Mapping(source = "team.id", target = "teamId")
    PlayerTeamResponse toResponse(PlayerTeam playerTeam);
}
