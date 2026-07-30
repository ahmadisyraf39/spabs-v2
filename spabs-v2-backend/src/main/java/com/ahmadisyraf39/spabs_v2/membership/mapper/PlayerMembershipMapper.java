package com.ahmadisyraf39.spabs_v2.membership.mapper;

import com.ahmadisyraf39.spabs_v2.membership.dto.request.PlayerMembershipRequest;
import com.ahmadisyraf39.spabs_v2.membership.dto.response.PlayerMembershipResponse;
import com.ahmadisyraf39.spabs_v2.membership.entity.PlayerMembership;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PlayerMembershipMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "player", ignore = true)
    PlayerMembership toEntity(PlayerMembershipRequest request);

    @InheritConfiguration(name = "toEntity")
    void updateEntity(@MappingTarget PlayerMembership playerMembership, PlayerMembershipRequest request);

    @Mapping(source = "player.id", target = "playerId")
    PlayerMembershipResponse toResponse(PlayerMembership playerMembership);
}
