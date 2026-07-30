package com.ahmadisyraf39.spabs_v2.finance.dto.response;

import com.ahmadisyraf39.spabs_v2.finance.entity.enums.CoachPaymentType;
import com.ahmadisyraf39.spabs_v2.finance.entity.enums.PaymentStatus;
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
public class CoachPaymentResponse {

    private Long id;
    private Long coachId;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private PaymentStatus status;
    private CoachPaymentType paymentType;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
