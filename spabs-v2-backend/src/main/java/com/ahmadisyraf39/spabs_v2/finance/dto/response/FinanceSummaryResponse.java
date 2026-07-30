package com.ahmadisyraf39.spabs_v2.finance.dto.response;

import com.ahmadisyraf39.spabs_v2.finance.entity.enums.FinanceCategory;
import java.math.BigDecimal;
import java.util.Map;
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
public class FinanceSummaryResponse {

    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netBalance;
    private Map<FinanceCategory, BigDecimal> byCategory;
}
