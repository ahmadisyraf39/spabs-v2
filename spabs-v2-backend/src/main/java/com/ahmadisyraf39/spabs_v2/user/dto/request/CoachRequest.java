package com.ahmadisyraf39.spabs_v2.user.dto.request;

import com.ahmadisyraf39.spabs_v2.user.entity.enums.CoachCertification;
import com.ahmadisyraf39.spabs_v2.user.entity.enums.CoachSpecialization;
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
public class CoachRequest {

    @NotNull
    private Long userId;

    @NotNull
    private CoachSpecialization specialization;

    @NotNull
    private CoachCertification certification;
}
