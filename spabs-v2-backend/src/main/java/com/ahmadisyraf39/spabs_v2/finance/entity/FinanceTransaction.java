package com.ahmadisyraf39.spabs_v2.finance.entity;

import com.ahmadisyraf39.spabs_v2.common.entity.BaseEntity;
import com.ahmadisyraf39.spabs_v2.finance.entity.enums.FinanceCategory;
import com.ahmadisyraf39.spabs_v2.finance.entity.enums.FinanceReferenceType;
import com.ahmadisyraf39.spabs_v2.finance.entity.enums.FinanceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "finance_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class FinanceTransaction extends BaseEntity {

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "finance_type", nullable = false)
    private FinanceType financeType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "finance_category", nullable = false)
    private FinanceCategory financeCategory;

    @NotNull
    @DecimalMin("0.00")
    @Column(nullable = false)
    private BigDecimal amount;

    @Column(length = 300)
    private String description;

    @NotNull
    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    private Long referenceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type")
    private FinanceReferenceType referenceType;
}
