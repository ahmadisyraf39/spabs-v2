package com.ahmadisyraf39.spabs_v2.finance.entity;

import com.ahmadisyraf39.spabs_v2.common.entity.BaseEntity;
import com.ahmadisyraf39.spabs_v2.finance.entity.enums.CoachPaymentType;
import com.ahmadisyraf39.spabs_v2.finance.entity.enums.PaymentStatus;
import com.ahmadisyraf39.spabs_v2.user.entity.Coach;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "coach_payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CoachPayment extends BaseEntity {

    @NotNull
    @ManyToOne
    @JoinColumn(name = "coach_id", nullable = false)
    private Coach coach;

    @NotNull
    @DecimalMin("0.00")
    @Column(nullable = false)
    private BigDecimal amount;

    @NotNull
    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false)
    private CoachPaymentType paymentType;

    private String remarks;
}
