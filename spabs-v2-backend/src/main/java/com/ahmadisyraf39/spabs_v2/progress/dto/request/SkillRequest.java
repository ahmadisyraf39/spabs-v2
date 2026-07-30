package com.ahmadisyraf39.spabs_v2.progress.dto.request;

import com.ahmadisyraf39.spabs_v2.team.entity.enums.AgeGroup;
import com.ahmadisyraf39.spabs_v2.team.entity.enums.TeamCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class SkillRequest {

    @NotNull
    private AgeGroup ageGroup;

    @NotNull
    private TeamCategory category;

    @NotBlank
    private String name;

    private String description;
}
