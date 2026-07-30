package com.ahmadisyraf39.spabs_v2.membership.dto.response;

import com.ahmadisyraf39.spabs_v2.membership.entity.enums.CoachMembershipStatus;
import java.time.LocalDate;
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
public class CoachMembershipResponse {

    private Long id;
    private Long coachId;
    private CoachMembershipStatus status;
    private LocalDate joinedAt;
    private LocalDate leftAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
