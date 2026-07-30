package com.ahmadisyraf39.spabs_v2.sponsorship.mapper;

import com.ahmadisyraf39.spabs_v2.sponsorship.dto.request.SponsorRequest;
import com.ahmadisyraf39.spabs_v2.sponsorship.dto.response.SponsorResponse;
import com.ahmadisyraf39.spabs_v2.sponsorship.entity.Sponsor;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SponsorMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Sponsor toEntity(SponsorRequest request);

    @InheritConfiguration(name = "toEntity")
    void updateEntity(@MappingTarget Sponsor sponsor, SponsorRequest request);

    SponsorResponse toResponse(Sponsor sponsor);
}
