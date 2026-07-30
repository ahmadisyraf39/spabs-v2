package com.ahmadisyraf39.spabs_v2.inventory.repository;

import com.ahmadisyraf39.spabs_v2.inventory.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
}
