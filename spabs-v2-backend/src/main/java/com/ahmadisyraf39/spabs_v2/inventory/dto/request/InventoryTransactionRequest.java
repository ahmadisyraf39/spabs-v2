package com.ahmadisyraf39.spabs_v2.inventory.dto.request;

import com.ahmadisyraf39.spabs_v2.inventory.entity.enums.InventoryTransactionType;
import jakarta.validation.constraints.Min;
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
public class InventoryTransactionRequest {

    @NotNull
    private Long inventoryId;

    @NotNull
    private InventoryTransactionType transactionType;

    @NotNull
    @Min(0)
    private Integer quantity;

    @NotNull
    private LocalDate transactionDate;

    private BigDecimal price;

    private String remarks;
}
