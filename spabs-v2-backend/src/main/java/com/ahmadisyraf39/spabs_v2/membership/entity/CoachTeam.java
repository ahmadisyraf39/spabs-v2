package com.ahmadisyraf39.spabs_v2.membership.entity;

import com.ahmadisyraf39.spabs_v2.common.entity.BaseEntity;
import com.ahmadisyraf39.spabs_v2.membership.entity.enums.CoachTeamRole;
import com.ahmadisyraf39.spabs_v2.membership.entity.enums.CoachTeamStatus;
import com.ahmadisyraf39.spabs_v2.team.entity.Team;
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
@Table(name = "coach_teams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CoachTeam extends BaseEntity {

    @NotNull
    @ManyToOne
    @JoinColumn(name = "coach_id", nullable = false)
    private Coach coach;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CoachTeamRole role;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CoachTeamStatus status;

    @NotNull
    @Column(nullable = false)
    private LocalDate joinedAt;

    private LocalDate leftAt;
}
