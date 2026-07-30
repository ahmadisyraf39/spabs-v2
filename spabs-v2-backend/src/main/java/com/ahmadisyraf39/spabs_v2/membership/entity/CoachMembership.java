package com.ahmadisyraf39.spabs_v2.membership.entity;

import com.ahmadisyraf39.spabs_v2.common.entity.BaseEntity;
import com.ahmadisyraf39.spabs_v2.membership.entity.enums.CoachMembershipStatus;
import com.ahmadisyraf39.spabs_v2.user.entity.Coach;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "coach_memberships")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CoachMembership extends BaseEntity {

    @NotNull
    @ManyToOne
    @JoinColumn(name = "coach_id", nullable = false)
    private Coach coach;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CoachMembershipStatus status;

    @NotNull
    @Column(nullable = false)
    private LocalDate joinedAt;

    private LocalDate leftAt;
}
