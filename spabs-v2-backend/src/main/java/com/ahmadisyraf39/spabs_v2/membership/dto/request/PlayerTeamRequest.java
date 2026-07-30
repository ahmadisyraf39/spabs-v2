package com.ahmadisyraf39.spabs_v2.membership.dto.request;

import com.ahmadisyraf39.spabs_v2.membership.entity.enums.PlayerTeamStatus;
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
public class PlayerTeamRequest {

    @NotNull
    private Long playerId;

    @NotNull
    private Long teamId;

    private Integer jerseyNumber;

    @NotNull
    private PlayerTeamStatus status;

    @NotNull
    private LocalDate joinedAt;

    private LocalDate leftAt;
}
