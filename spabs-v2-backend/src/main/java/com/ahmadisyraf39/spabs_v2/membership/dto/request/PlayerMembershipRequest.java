package com.ahmadisyraf39.spabs_v2.membership.dto.request;

import com.ahmadisyraf39.spabs_v2.membership.entity.enums.PlayerMembershipStatus;
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
public class PlayerMembershipRequest {

    @NotNull
    private Long playerId;

    @NotNull
    private PlayerMembershipStatus status;

    @NotNull
    private LocalDate joinedAt;

    private LocalDate leftAt;
}
