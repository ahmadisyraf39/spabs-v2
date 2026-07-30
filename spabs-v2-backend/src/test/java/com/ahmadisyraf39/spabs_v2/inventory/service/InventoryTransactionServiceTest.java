package com.ahmadisyraf39.spabs_v2.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ahmadisyraf39.spabs_v2.finance.entity.enums.FinanceCategory;
import com.ahmadisyraf39.spabs_v2.finance.entity.enums.FinanceReferenceType;
import com.ahmadisyraf39.spabs_v2.finance.entity.enums.FinanceType;
import com.ahmadisyraf39.spabs_v2.finance.service.FinanceTransactionService;
import com.ahmadisyraf39.spabs_v2.inventory.dto.request.InventoryTransactionRequest;
import com.ahmadisyraf39.spabs_v2.inventory.dto.response.InventoryTransactionResponse;
import com.ahmadisyraf39.spabs_v2.inventory.entity.Inventory;
import com.ahmadisyraf39.spabs_v2.inventory.entity.InventoryTransaction;
import com.ahmadisyraf39.spabs_v2.inventory.entity.enums.InventoryCategory;
import com.ahmadisyraf39.spabs_v2.inventory.entity.enums.InventoryTransactionType;
import com.ahmadisyraf39.spabs_v2.inventory.mapper.InventoryTransactionMapper;
import com.ahmadisyraf39.spabs_v2.inventory.repository.InventoryRepository;
import com.ahmadisyraf39.spabs_v2.inventory.repository.InventoryTransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InventoryTransactionServiceTest {

    @Mock
    private InventoryTransactionRepository inventoryTransactionRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryTransactionMapper inventoryTransactionMapper;

    @Mock
    private FinanceTransactionService financeTransactionService;

    @InjectMocks
    private InventoryTransactionService inventoryTransactionService;

    private Inventory inventoryWithQuantity(int quantity) {
        return Inventory.builder()
                .id(1L)
                .name("Footballs")
                .category(InventoryCategory.BALL)
                .currentQuantity(quantity)
                .build();
    }

    private void stubMapperPassthrough() {
        when(inventoryTransactionMapper.toEntity(any())).thenReturn(new InventoryTransaction());
        when(inventoryTransactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(inventoryTransactionMapper.toResponse(any()))
                .thenReturn(InventoryTransactionResponse.builder().build());
    }

    @Test
    void recordTransaction_initialStock_addsToQuantity() {
        Inventory inventory = inventoryWithQuantity(0);
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory));
        stubMapperPassthrough();

        InventoryTransactionRequest request = InventoryTransactionRequest.builder()
                .inventoryId(1L)
                .transactionType(InventoryTransactionType.INITIAL_STOCK)
                .quantity(20)
                .transactionDate(LocalDate.now())
                .build();

        inventoryTransactionService.recordTransaction(request);

        assertThat(inventory.getCurrentQuantity()).isEqualTo(20);
    }

    @Test
    void recordTransaction_purchase_addsToQuantityAndRecordsLinkedExpenseWhenPriced() {
        Inventory inventory = inventoryWithQuantity(20);
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory));
        stubMapperPassthrough();

        InventoryTransactionRequest request = InventoryTransactionRequest.builder()
                .inventoryId(1L)
                .transactionType(InventoryTransactionType.PURCHASE)
                .quantity(10)
                .transactionDate(LocalDate.now())
                .price(new BigDecimal("450.00"))
                .build();

        inventoryTransactionService.recordTransaction(request);

        assertThat(inventory.getCurrentQuantity()).isEqualTo(30);
        verify(financeTransactionService)
                .recordLinked(
                        eq(FinanceType.EXPENSE),
                        eq(FinanceCategory.INVENTORY_PURCHASE),
                        eq(new BigDecimal("450.00")),
                        any(),
                        eq(request.getTransactionDate()),
                        any(),
                        eq(FinanceReferenceType.INVENTORY));
    }

    @Test
    void recordTransaction_purchaseWithoutPrice_doesNotRecordLinkedExpense() {
        Inventory inventory = inventoryWithQuantity(20);
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory));
        stubMapperPassthrough();

        InventoryTransactionRequest request = InventoryTransactionRequest.builder()
                .inventoryId(1L)
                .transactionType(InventoryTransactionType.PURCHASE)
                .quantity(10)
                .transactionDate(LocalDate.now())
                .build();

        inventoryTransactionService.recordTransaction(request);

        assertThat(inventory.getCurrentQuantity()).isEqualTo(30);
        verify(financeTransactionService, never()).recordLinked(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void recordTransaction_damage_subtractsFromQuantity() {
        Inventory inventory = inventoryWithQuantity(20);
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory));
        stubMapperPassthrough();

        InventoryTransactionRequest request = InventoryTransactionRequest.builder()
                .inventoryId(1L)
                .transactionType(InventoryTransactionType.DAMAGE)
                .quantity(5)
                .transactionDate(LocalDate.now())
                .build();

        inventoryTransactionService.recordTransaction(request);

        assertThat(inventory.getCurrentQuantity()).isEqualTo(15);
    }

    @Test
    void recordTransaction_damageExceedingStock_flooredAtZero() {
        Inventory inventory = inventoryWithQuantity(2);
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory));
        stubMapperPassthrough();

        InventoryTransactionRequest request = InventoryTransactionRequest.builder()
                .inventoryId(1L)
                .transactionType(InventoryTransactionType.LOST)
                .quantity(5)
                .transactionDate(LocalDate.now())
                .build();

        inventoryTransactionService.recordTransaction(request);

        assertThat(inventory.getCurrentQuantity()).isEqualTo(0);
    }

    @Test
    void recordTransaction_adjustment_setsQuantityDirectlyRatherThanAdding() {
        Inventory inventory = inventoryWithQuantity(20);
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory));
        stubMapperPassthrough();

        InventoryTransactionRequest request = InventoryTransactionRequest.builder()
                .inventoryId(1L)
                .transactionType(InventoryTransactionType.ADJUSTMENT)
                .quantity(7)
                .transactionDate(LocalDate.now())
                .build();

        inventoryTransactionService.recordTransaction(request);

        assertThat(inventory.getCurrentQuantity()).isEqualTo(7);
    }
}
