package com.ahmadisyraf39.spabs_v2.sponsorship.service;

import com.ahmadisyraf39.spabs_v2.common.exception.ResourceNotFoundException;
import com.ahmadisyraf39.spabs_v2.finance.entity.enums.FinanceCategory;
import com.ahmadisyraf39.spabs_v2.finance.entity.enums.FinanceReferenceType;
import com.ahmadisyraf39.spabs_v2.finance.entity.enums.FinanceType;
import com.ahmadisyraf39.spabs_v2.finance.service.FinanceTransactionService;
import com.ahmadisyraf39.spabs_v2.sponsorship.dto.request.ClubSponsorshipRequest;
import com.ahmadisyraf39.spabs_v2.sponsorship.dto.response.ClubSponsorshipResponse;
import com.ahmadisyraf39.spabs_v2.sponsorship.entity.ClubSponsorship;
import com.ahmadisyraf39.spabs_v2.sponsorship.entity.Sponsor;
import com.ahmadisyraf39.spabs_v2.sponsorship.mapper.ClubSponsorshipMapper;
import com.ahmadisyraf39.spabs_v2.sponsorship.repository.ClubSponsorshipRepository;
import com.ahmadisyraf39.spabs_v2.sponsorship.repository.SponsorRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClubSponsorshipService {

    private final ClubSponsorshipRepository clubSponsorshipRepository;
    private final SponsorRepository sponsorRepository;
    private final ClubSponsorshipMapper clubSponsorshipMapper;
    private final FinanceTransactionService financeTransactionService;

    public ClubSponsorshipResponse create(ClubSponsorshipRequest request) {
        Sponsor sponsor = findSponsorById(request.getSponsorId());
        ClubSponsorship clubSponsorship = clubSponsorshipMapper.toEntity(request);
        clubSponsorship.setSponsor(sponsor);
        ClubSponsorship saved = clubSponsorshipRepository.save(clubSponsorship);

        financeTransactionService.recordLinked(
                FinanceType.INCOME,
                FinanceCategory.SPONSORSHIP,
                saved.getAmount(),
                sponsor.getName() + " sponsorship",
                saved.getStartDate(),
                saved.getId(),
                FinanceReferenceType.SPONSORSHIP);

        return clubSponsorshipMapper.toResponse(saved);
    }

    public ClubSponsorshipResponse getById(Long id) {
        return clubSponsorshipMapper.toResponse(findEntityById(id));
    }

    public List<ClubSponsorshipResponse> getAll() {
        return clubSponsorshipRepository.findAll().stream()
                .map(clubSponsorshipMapper::toResponse)
                .toList();
    }

    public List<ClubSponsorshipResponse> getBySponsor(Long sponsorId) {
        return clubSponsorshipRepository.findBySponsorId(sponsorId).stream()
                .map(clubSponsorshipMapper::toResponse)
                .toList();
    }

    public ClubSponsorshipResponse update(Long id, ClubSponsorshipRequest request) {
        ClubSponsorship clubSponsorship = findEntityById(id);
        clubSponsorshipMapper.updateEntity(clubSponsorship, request);
        clubSponsorship.setSponsor(findSponsorById(request.getSponsorId()));
        return clubSponsorshipMapper.toResponse(clubSponsorshipRepository.save(clubSponsorship));
    }

    public void delete(Long id) {
        clubSponsorshipRepository.delete(findEntityById(id));
    }

    private ClubSponsorship findEntityById(Long id) {
        return clubSponsorshipRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ClubSponsorship not found: " + id));
    }

    private Sponsor findSponsorById(Long sponsorId) {
        return sponsorRepository
                .findById(sponsorId)
                .orElseThrow(() -> new ResourceNotFoundException("Sponsor not found: " + sponsorId));
    }
}
