package com.ahmadisyraf39.spabs_v2.membership.mapper;

import com.ahmadisyraf39.spabs_v2.membership.dto.request.CoachTeamRequest;
import com.ahmadisyraf39.spabs_v2.membership.dto.response.CoachTeamResponse;
import com.ahmadisyraf39.spabs_v2.membership.entity.CoachTeam;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CoachTeamMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "coach", ignore = true)
    @Mapping(target = "team", ignore = true)
    CoachTeam toEntity(CoachTeamRequest request);

    @InheritConfiguration(name = "toEntity")
    void updateEntity(@MappingTarget CoachTeam coachTeam, CoachTeamRequest request);

    @Mapping(source = "coach.id", target = "coachId")
    @Mapping(source = "team.id", target = "teamId")
    CoachTeamResponse toResponse(CoachTeam coachTeam);
}
