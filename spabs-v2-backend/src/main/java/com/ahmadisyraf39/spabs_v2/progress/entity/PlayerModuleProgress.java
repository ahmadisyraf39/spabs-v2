package com.ahmadisyraf39.spabs_v2.progress.entity;

import com.ahmadisyraf39.spabs_v2.common.entity.BaseEntity;
import com.ahmadisyraf39.spabs_v2.player.entity.Player;
import com.ahmadisyraf39.spabs_v2.progress.entity.enums.ModuleProgressStatus;
import com.ahmadisyraf39.spabs_v2.user.entity.Coach;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
        name = "player_module_progress",
        uniqueConstraints = @UniqueConstraint(columnNames = {"module_id", "player_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PlayerModuleProgress extends BaseEntity {

    @NotNull
    @ManyToOne
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModuleProgressStatus status;

    @ManyToOne
    @JoinColumn(name = "recorded_by_coach_id")
    private Coach recordedByCoach;
}
