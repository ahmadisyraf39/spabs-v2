package com.ahmadisyraf39.spabs_v2.sponsorship.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ahmadisyraf39.spabs_v2.finance.entity.enums.FinanceCategory;
import com.ahmadisyraf39.spabs_v2.finance.entity.enums.FinanceReferenceType;
import com.ahmadisyraf39.spabs_v2.finance.entity.enums.FinanceType;
import com.ahmadisyraf39.spabs_v2.finance.service.FinanceTransactionService;
import com.ahmadisyraf39.spabs_v2.sponsorship.dto.request.ClubSponsorshipRequest;
import com.ahmadisyraf39.spabs_v2.sponsorship.dto.response.ClubSponsorshipResponse;
import com.ahmadisyraf39.spabs_v2.sponsorship.entity.ClubSponsorship;
import com.ahmadisyraf39.spabs_v2.sponsorship.entity.Sponsor;
import com.ahmadisyraf39.spabs_v2.sponsorship.entity.enums.SponsorshipType;
import com.ahmadisyraf39.spabs_v2.sponsorship.mapper.ClubSponsorshipMapper;
import com.ahmadisyraf39.spabs_v2.sponsorship.repository.ClubSponsorshipRepository;
import com.ahmadisyraf39.spabs_v2.sponsorship.repository.SponsorRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClubSponsorshipServiceTest {

    @Mock
    private ClubSponsorshipRepository clubSponsorshipRepository;

    @Mock
    private SponsorRepository sponsorRepository;

    @Mock
    private ClubSponsorshipMapper clubSponsorshipMapper;

    @Mock
    private FinanceTransactionService financeTransactionService;

    @InjectMocks
    private ClubSponsorshipService clubSponsorshipService;

    @Test
    void create_recordsLinkedIncomeImmediatelyNoPaidUnpaidStaging() {
        Sponsor sponsor = Sponsor.builder().id(1L).name("Local Sports Store").build();
        when(sponsorRepository.findById(1L)).thenReturn(Optional.of(sponsor));
        ClubSponsorship entity = ClubSponsorship.builder()
                .amount(new BigDecimal("2000.00"))
                .startDate(LocalDate.of(2026, 1, 1))
                .build();
        when(clubSponsorshipMapper.toEntity(any())).thenReturn(entity);
        when(clubSponsorshipRepository.save(entity)).thenReturn(entity);
        when(clubSponsorshipMapper.toResponse(entity)).thenReturn(ClubSponsorshipResponse.builder().build());

        ClubSponsorshipRequest request = ClubSponsorshipRequest.builder()
                .sponsorId(1L)
                .sponsorshipType(SponsorshipType.CASH)
                .amount(new BigDecimal("2000.00"))
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 12, 31))
                .build();

        clubSponsorshipService.create(request);

        assertThat(entity.getSponsor()).isEqualTo(sponsor);
        verify(financeTransactionService)
                .recordLinked(
                        eq(FinanceType.INCOME),
                        eq(FinanceCategory.SPONSORSHIP),
                        eq(new BigDecimal("2000.00")),
                        eq("Local Sports Store sponsorship"),
                        eq(LocalDate.of(2026, 1, 1)),
                        any(),
                        eq(FinanceReferenceType.SPONSORSHIP));
    }

    @Test
    void update_reResolvesSponsorFromRequest() {
        ClubSponsorship entity = new ClubSponsorship();
        when(clubSponsorshipRepository.findById(1L)).thenReturn(Optional.of(entity));
        Sponsor newSponsor = Sponsor.builder().id(2L).name("City Cafe").build();
        when(sponsorRepository.findById(2L)).thenReturn(Optional.of(newSponsor));
        when(clubSponsorshipRepository.save(entity)).thenReturn(entity);
        when(clubSponsorshipMapper.toResponse(entity)).thenReturn(ClubSponsorshipResponse.builder().build());

        ClubSponsorshipRequest request = ClubSponsorshipRequest.builder()
                .sponsorId(2L)
                .sponsorshipType(SponsorshipType.CASH)
                .amount(new BigDecimal("1000.00"))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusYears(1))
                .build();

        clubSponsorshipService.update(1L, request);

        assertThat(entity.getSponsor()).isEqualTo(newSponsor);
    }
}
