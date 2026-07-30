package com.ahmadisyraf39.spabs_v2.sponsorship.dto.response;

import com.ahmadisyraf39.spabs_v2.sponsorship.entity.enums.SponsorshipType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClubSponsorshipResponse {

    private Long id;
    private Long sponsorId;
    private String sponsorName;
    private SponsorshipType sponsorshipType;
    private BigDecimal amount;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
