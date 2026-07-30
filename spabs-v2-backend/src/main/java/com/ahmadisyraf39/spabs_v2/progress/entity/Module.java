package com.ahmadisyraf39.spabs_v2.progress.entity;

import com.ahmadisyraf39.spabs_v2.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "modules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Module extends BaseEntity {

    @NotNull
    @ManyToOne
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column(name = "criteria_25")
    private String criteria25;

    @Column(name = "criteria_50")
    private String criteria50;

    @Column(name = "criteria_75")
    private String criteria75;

    @Column(name = "criteria_100")
    private String criteria100;
}
