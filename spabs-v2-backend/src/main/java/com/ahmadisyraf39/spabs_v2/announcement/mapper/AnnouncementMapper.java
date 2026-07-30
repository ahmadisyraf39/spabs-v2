package com.ahmadisyraf39.spabs_v2.announcement.mapper;

import com.ahmadisyraf39.spabs_v2.announcement.dto.request.AnnouncementRequest;
import com.ahmadisyraf39.spabs_v2.announcement.dto.response.AnnouncementResponse;
import com.ahmadisyraf39.spabs_v2.announcement.entity.Announcement;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AnnouncementMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "team", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    Announcement toEntity(AnnouncementRequest request);

    @InheritConfiguration(name = "toEntity")
    void updateEntity(@MappingTarget Announcement announcement, AnnouncementRequest request);

    @Mapping(source = "team.id", target = "teamId")
    @Mapping(source = "createdBy.id", target = "createdByUserId")
    AnnouncementResponse toResponse(Announcement announcement);
}
