package com.ahmadisyraf39.spabs_v2.team.mapper;

import com.ahmadisyraf39.spabs_v2.team.dto.request.TeamRequest;
import com.ahmadisyraf39.spabs_v2.team.dto.response.TeamResponse;
import com.ahmadisyraf39.spabs_v2.team.entity.Team;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TeamMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Team toEntity(TeamRequest request);

    @InheritConfiguration(name = "toEntity")
    void updateEntity(@MappingTarget Team team, TeamRequest request);

    TeamResponse toResponse(Team team);
}
