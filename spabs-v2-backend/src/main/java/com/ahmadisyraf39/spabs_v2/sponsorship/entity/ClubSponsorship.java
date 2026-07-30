package com.ahmadisyraf39.spabs_v2.sponsorship.entity;

import com.ahmadisyraf39.spabs_v2.common.entity.BaseEntity;
import com.ahmadisyraf39.spabs_v2.sponsorship.entity.enums.SponsorshipType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "club_sponsorships")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ClubSponsorship extends BaseEntity {

    @NotNull
    @ManyToOne
    @JoinColumn(name = "sponsor_id", nullable = false)
    private Sponsor sponsor;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "sponsorship_type", nullable = false)
    private SponsorshipType sponsorshipType;

    @NotNull
    @DecimalMin("0.00")
    @Column(nullable = false)
    private BigDecimal amount;

    @Column(length = 500)
    private String description;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
}
