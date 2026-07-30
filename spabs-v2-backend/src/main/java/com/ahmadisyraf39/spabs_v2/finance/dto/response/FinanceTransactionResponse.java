package com.ahmadisyraf39.spabs_v2.finance.dto.response;

import com.ahmadisyraf39.spabs_v2.finance.entity.enums.FinanceCategory;
import com.ahmadisyraf39.spabs_v2.finance.entity.enums.FinanceReferenceType;
import com.ahmadisyraf39.spabs_v2.finance.entity.enums.FinanceType;
import java.math.BigDecimal;
import java.time.LocalDate;
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
public class FinanceTransactionResponse {

    private Long id;
    private FinanceType financeType;
    private FinanceCategory financeCategory;
    private BigDecimal amount;
    private String description;
    private LocalDate transactionDate;
    private Long referenceId;
    private FinanceReferenceType referenceType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
