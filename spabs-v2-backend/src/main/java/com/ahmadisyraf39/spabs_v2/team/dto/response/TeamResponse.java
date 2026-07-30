package com.ahmadisyraf39.spabs_v2.team.dto.response;

import com.ahmadisyraf39.spabs_v2.team.entity.enums.AgeGroup;
import com.ahmadisyraf39.spabs_v2.team.entity.enums.TeamCategory;
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
public class TeamResponse {

    private Long id;
    private String name;
    private TeamCategory category;
    private AgeGroup ageGroup;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
