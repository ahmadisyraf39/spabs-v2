package com.ahmadisyraf39.spabs_v2.attendance.mapper;

import com.ahmadisyraf39.spabs_v2.attendance.dto.request.AttendanceRequest;
import com.ahmadisyraf39.spabs_v2.attendance.dto.response.AttendanceResponse;
import com.ahmadisyraf39.spabs_v2.attendance.entity.Attendance;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AttendanceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "activity", ignore = true)
    @Mapping(target = "player", ignore = true)
    Attendance toEntity(AttendanceRequest request);

    @InheritConfiguration(name = "toEntity")
    void updateEntity(@MappingTarget Attendance attendance, AttendanceRequest request);

    @Mapping(source = "activity.id", target = "activityId")
    @Mapping(source = "player.id", target = "playerId")
    AttendanceResponse toResponse(Attendance attendance);
}
