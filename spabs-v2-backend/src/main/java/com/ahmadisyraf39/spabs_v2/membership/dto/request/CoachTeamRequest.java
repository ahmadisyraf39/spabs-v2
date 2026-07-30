package com.ahmadisyraf39.spabs_v2.membership.dto.request;

import com.ahmadisyraf39.spabs_v2.membership.entity.enums.CoachTeamRole;
import com.ahmadisyraf39.spabs_v2.membership.entity.enums.CoachTeamStatus;
import jakarta.validation.constraints.NotNull;
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
public class CoachTeamRequest {

    @NotNull
    private Long coachId;

    @NotNull
    private Long teamId;

    @NotNull
    private CoachTeamRole role;

    @NotNull
    private CoachTeamStatus status;

    @NotNull
    private LocalDate joinedAt;

    private LocalDate leftAt;
}
