package com.ahmadisyraf39.spabs_v2.user.mapper;

import com.ahmadisyraf39.spabs_v2.user.dto.request.AdminRequest;
import com.ahmadisyraf39.spabs_v2.user.dto.response.AdminResponse;
import com.ahmadisyraf39.spabs_v2.user.entity.Admin;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AdminMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    Admin toEntity(AdminRequest request);

    @InheritConfiguration(name = "toEntity")
    void updateEntity(@MappingTarget Admin admin, AdminRequest request);

    @Mapping(source = "user.id", target = "userId")
    AdminResponse toResponse(Admin admin);
}
