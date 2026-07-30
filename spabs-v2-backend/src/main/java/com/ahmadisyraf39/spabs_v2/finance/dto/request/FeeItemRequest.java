package com.ahmadisyraf39.spabs_v2.finance.dto.request;

import com.ahmadisyraf39.spabs_v2.finance.entity.enums.FeeType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
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
public class FeeItemRequest {

    @NotBlank
    private String name;

    @NotNull
    private FeeType feeType;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal amount;
}
