package com.ahmadisyraf39.spabs_v2.membership.entity;

import com.ahmadisyraf39.spabs_v2.common.entity.BaseEntity;
import com.ahmadisyraf39.spabs_v2.membership.entity.enums.PlayerMembershipStatus;
import com.ahmadisyraf39.spabs_v2.player.entity.Player;
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
@Table(name = "player_memberships")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PlayerMembership extends BaseEntity {

    @NotNull
    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerMembershipStatus status;

    @NotNull
    @Column(nullable = false)
    private LocalDate joinedAt;

    private LocalDate leftAt;
}
