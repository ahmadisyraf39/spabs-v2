package com.ahmadisyraf39.spabs_v2.progress.dto.request;

import com.ahmadisyraf39.spabs_v2.progress.entity.enums.ModuleProgressStatus;
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
public class PlayerModuleProgressRequest {

    @NotNull
    private Long moduleId;

    @NotNull
    private Long playerId;

    @NotNull
    private Long teamId;

    @NotNull
    private ModuleProgressStatus status;
}
