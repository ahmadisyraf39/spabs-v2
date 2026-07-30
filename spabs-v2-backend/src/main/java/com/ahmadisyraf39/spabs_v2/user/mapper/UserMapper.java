package com.ahmadisyraf39.spabs_v2.user.mapper;

import com.ahmadisyraf39.spabs_v2.user.dto.request.UserRequest;
import com.ahmadisyraf39.spabs_v2.user.dto.response.UserResponse;
import com.ahmadisyraf39.spabs_v2.user.entity.User;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "mustChangePassword", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "password", ignore = true)
    User toEntity(UserRequest request);

    @InheritConfiguration(name = "toEntity")
    void updateEntity(@MappingTarget User user, UserRequest request);

    UserResponse toResponse(User user);
}
