package com.ahmadisyraf39.spabs_v2.sponsorship.dto.request;

import com.ahmadisyraf39.spabs_v2.sponsorship.entity.enums.SponsorshipType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
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
public class ClubSponsorshipRequest {

    @NotNull
    private Long sponsorId;

    @NotNull
    private SponsorshipType sponsorshipType;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal amount;

    private String description;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;
}
