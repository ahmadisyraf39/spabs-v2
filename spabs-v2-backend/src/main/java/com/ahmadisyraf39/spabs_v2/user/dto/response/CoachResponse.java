package com.ahmadisyraf39.spabs_v2.user.dto.response;

import com.ahmadisyraf39.spabs_v2.user.entity.enums.CoachCertification;
import com.ahmadisyraf39.spabs_v2.user.entity.enums.CoachSpecialization;
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
public class CoachResponse {

    private Long id;
    private Long userId;
    private CoachSpecialization specialization;
    private CoachCertification certification;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
