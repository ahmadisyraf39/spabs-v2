package com.ahmadisyraf39.spabs_v2.sponsorship.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.ahmadisyraf39.spabs_v2.common.exception.ResourceNotFoundException;
import com.ahmadisyraf39.spabs_v2.sponsorship.dto.request.SponsorRequest;
import com.ahmadisyraf39.spabs_v2.sponsorship.dto.response.SponsorResponse;
import com.ahmadisyraf39.spabs_v2.sponsorship.entity.Sponsor;
import com.ahmadisyraf39.spabs_v2.sponsorship.mapper.SponsorMapper;
import com.ahmadisyraf39.spabs_v2.sponsorship.repository.SponsorRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SponsorServiceTest {

    @Mock
    private SponsorRepository sponsorRepository;

    @Mock
    private SponsorMapper sponsorMapper;

    @InjectMocks
    private SponsorService sponsorService;

    @Test
    void create_savesAndReturnsMappedResponse() {
        Sponsor entity = new Sponsor();
        when(sponsorMapper.toEntity(org.mockito.ArgumentMatchers.any())).thenReturn(entity);
        when(sponsorRepository.save(entity)).thenReturn(entity);
        when(sponsorMapper.toResponse(entity)).thenReturn(SponsorResponse.builder().build());

        SponsorRequest request = SponsorRequest.builder().name("Local Sports Store").build();

        assertThat(sponsorService.create(request)).isNotNull();
    }

    @Test
    void getById_notFound_throwsResourceNotFoundException() {
        when(sponsorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sponsorService.getById(1L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_notFound_throwsResourceNotFoundException() {
        when(sponsorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sponsorService.delete(1L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
