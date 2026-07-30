package com.ahmadisyraf39.spabs_v2.finance.dto.response;

import com.ahmadisyraf39.spabs_v2.finance.entity.enums.FeeType;
import java.math.BigDecimal;
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
public class FeeItemResponse {

    private Long id;
    private String name;
    private FeeType feeType;
    private BigDecimal amount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
