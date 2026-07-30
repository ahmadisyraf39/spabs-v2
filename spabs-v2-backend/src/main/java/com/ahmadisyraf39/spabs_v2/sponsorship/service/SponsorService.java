package com.ahmadisyraf39.spabs_v2.sponsorship.service;

import com.ahmadisyraf39.spabs_v2.common.exception.ResourceNotFoundException;
import com.ahmadisyraf39.spabs_v2.sponsorship.dto.request.SponsorRequest;
import com.ahmadisyraf39.spabs_v2.sponsorship.dto.response.SponsorResponse;
import com.ahmadisyraf39.spabs_v2.sponsorship.entity.Sponsor;
import com.ahmadisyraf39.spabs_v2.sponsorship.mapper.SponsorMapper;
import com.ahmadisyraf39.spabs_v2.sponsorship.repository.SponsorRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SponsorService {

    private final SponsorRepository sponsorRepository;
    private final SponsorMapper sponsorMapper;

    public SponsorResponse create(SponsorRequest request) {
        Sponsor sponsor = sponsorMapper.toEntity(request);
        return sponsorMapper.toResponse(sponsorRepository.save(sponsor));
    }

    public SponsorResponse getById(Long id) {
        return sponsorMapper.toResponse(findEntityById(id));
    }

    public List<SponsorResponse> getAll() {
        return sponsorRepository.findAll().stream().map(sponsorMapper::toResponse).toList();
    }

    public SponsorResponse update(Long id, SponsorRequest request) {
        Sponsor sponsor = findEntityById(id);
        sponsorMapper.updateEntity(sponsor, request);
        return sponsorMapper.toResponse(sponsorRepository.save(sponsor));
    }

    public void delete(Long id) {
        sponsorRepository.delete(findEntityById(id));
    }

    private Sponsor findEntityById(Long id) {
        return sponsorRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sponsor not found: " + id));
    }
}
