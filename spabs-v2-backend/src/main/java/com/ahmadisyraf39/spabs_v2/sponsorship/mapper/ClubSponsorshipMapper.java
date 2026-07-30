package com.ahmadisyraf39.spabs_v2.sponsorship.mapper;

import com.ahmadisyraf39.spabs_v2.sponsorship.dto.request.ClubSponsorshipRequest;
import com.ahmadisyraf39.spabs_v2.sponsorship.dto.response.ClubSponsorshipResponse;
import com.ahmadisyraf39.spabs_v2.sponsorship.entity.ClubSponsorship;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClubSponsorshipMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "sponsor", ignore = true)
    ClubSponsorship toEntity(ClubSponsorshipRequest request);

    @InheritConfiguration(name = "toEntity")
    void updateEntity(@MappingTarget ClubSponsorship clubSponsorship, ClubSponsorshipRequest request);

    @Mapping(source = "sponsor.id", target = "sponsorId")
    @Mapping(source = "sponsor.name", target = "sponsorName")
    ClubSponsorshipResponse toResponse(ClubSponsorship clubSponsorship);
}
