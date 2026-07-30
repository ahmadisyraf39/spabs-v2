package com.ahmadisyraf39.spabs_v2.progress.dto.response;

import com.ahmadisyraf39.spabs_v2.progress.entity.enums.ModuleProgressStatus;
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
public class PlayerModuleProgressResponse {

    private Long id;
    private Long moduleId;
    private Long playerId;
    private ModuleProgressStatus status;
    private Long recordedByCoachId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
