package com.ahmadisyraf39.spabs_v2.activity.dto.response;

import com.ahmadisyraf39.spabs_v2.activity.entity.enums.ActivityType;
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
public class ActivityResponse {

    private Long id;
    private Long teamId;
    private ActivityType type;
    private String title;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String location;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
