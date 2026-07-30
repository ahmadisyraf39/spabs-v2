package com.ahmadisyraf39.spabs_v2.finance.dto.request;

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
public class FeeRecordRequest {

    @NotNull
    private Long playerId;

    @NotNull
    private Long feeItemId;

    @NotNull
    private Long teamId;

    @NotNull
    private LocalDate dueDate;
}
