package com.ahmadisyraf39.spabs_v2.finance.dto.request;

import com.ahmadisyraf39.spabs_v2.finance.entity.enums.FinanceCategory;
import com.ahmadisyraf39.spabs_v2.finance.entity.enums.FinanceType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
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
public class ActivityFinanceRequest {

    @NotNull
    private Long activityId;

    @NotNull
    private FinanceType financeType;

    @NotNull
    private FinanceCategory financeCategory;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal amount;

    private String description;

    @NotNull
    private LocalDate transactionDate;
}
