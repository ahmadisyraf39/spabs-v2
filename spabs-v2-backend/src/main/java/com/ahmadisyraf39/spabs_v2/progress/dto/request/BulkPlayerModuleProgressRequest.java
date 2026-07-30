package com.ahmadisyraf39.spabs_v2.progress.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
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
public class BulkPlayerModuleProgressRequest {

    @NotNull
    private Long skillId;

    @NotNull
    private Long playerId;

    @NotNull
    private Long teamId;

    @NotEmpty
    @Valid
    private List<BulkPlayerModuleProgressEntry> entries;
}
