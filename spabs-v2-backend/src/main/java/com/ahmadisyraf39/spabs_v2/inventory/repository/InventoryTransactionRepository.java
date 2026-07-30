package com.ahmadisyraf39.spabs_v2.inventory.repository;

import com.ahmadisyraf39.spabs_v2.inventory.entity.InventoryTransaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {

    List<InventoryTransaction> findByInventoryId(Long inventoryId);
}
