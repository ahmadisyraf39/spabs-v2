package com.ahmadisyraf39.spabs_v2.inventory.dto.response;

import com.ahmadisyraf39.spabs_v2.inventory.entity.enums.InventoryTransactionType;
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
public class InventoryTransactionResponse {

    private Long id;
    private Long inventoryId;
    private String inventoryName;
    private InventoryTransactionType transactionType;
    private Integer quantity;
    private LocalDate transactionDate;
    private BigDecimal price;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
