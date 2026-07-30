package com.ahmadisyraf39.spabs_v2.finance.dto.request;

import com.ahmadisyraf39.spabs_v2.finance.entity.enums.CoachPaymentType;
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
public class CoachPaymentRequest {

    @NotNull
    private Long coachId;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal amount;

    @NotNull
    private LocalDate paymentDate;

    @NotNull
    private CoachPaymentType paymentType;

    private String remarks;
}
