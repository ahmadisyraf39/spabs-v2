package com.ahmadisyraf39.spabs_v2.inventory.dto.response;

import com.ahmadisyraf39.spabs_v2.inventory.entity.enums.InventoryCategory;
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
public class InventoryResponse {

    private Long id;
    private String name;
    private InventoryCategory category;
    private String description;
    private Integer currentQuantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
