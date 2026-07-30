package com.ahmadisyraf39.spabs_v2.activity.dto.request;

import com.ahmadisyraf39.spabs_v2.activity.entity.enums.ActivityType;
import jakarta.validation.constraints.NotNull;
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
public class ActivityRequest {

    @NotNull
    private Long teamId;

    @NotNull
    private ActivityType type;

    private String title;

    @NotNull
    private LocalDateTime startAt;

    private LocalDateTime endAt;

    private String location;

    private String description;
}
