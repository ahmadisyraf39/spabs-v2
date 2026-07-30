package com.ahmadisyraf39.spabs_v2.progress.dto.response;

import java.util.List;
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
public class SkillProgressResponse {

    private Long skillId;
    private String skillName;
    private List<ModuleProgressResponse> modules;
    private double skillPercentage;
}
