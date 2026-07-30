package com.ahmadisyraf39.spabs_v2.finance.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.ahmadisyraf39.spabs_v2.activity.repository.ActivityRepository;
import com.ahmadisyraf39.spabs_v2.common.exception.ResourceNotFoundException;
import com.ahmadisyraf39.spabs_v2.finance.dto.request.ActivityFinanceRequest;
import com.ahmadisyraf39.spabs_v2.finance.dto.response.FinanceSummaryResponse;
import com.ahmadisyraf39.spabs_v2.finance.dto.response.FinanceTransactionResponse;
import com.ahmadisyraf39.spabs_v2.finance.entity.FinanceTransaction;
import com.ahmadisyraf39.spabs_v2.finance.entity.enums.FinanceCategory;
import com.ahmadisyraf39.spabs_v2.finance.entity.enums.FinanceReferenceType;
import com.ahmadisyraf39.spabs_v2.finance.entity.enums.FinanceType;
import com.ahmadisyraf39.spabs_v2.finance.mapper.FinanceTransactionMapper;
import com.ahmadisyraf39.spabs_v2.finance.repository.FinanceTransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FinanceTransactionServiceTest {

    @Mock
    private FinanceTransactionRepository financeTransactionRepository;

    @Mock
    private FinanceTransactionMapper financeTransactionMapper;

    @Mock
    private ActivityRepository activityRepository;

    @InjectMocks
    private FinanceTransactionService financeTransactionService;

    private FinanceTransaction transaction(
            FinanceType type, FinanceCategory category, String amount, LocalDate date) {
        return FinanceTransaction.builder()
                .financeType(type)
                .financeCategory(category)
                .amount(new BigDecimal(amount))
                .transactionDate(date)
                .build();
    }

    @Test
    void getSummary_noDateFilter_includesEverythingAndComputesNetBalance() {
        when(financeTransactionRepository.findAll())
                .thenReturn(List.of(
                        transaction(FinanceType.INCOME, FinanceCategory.MONTHLY_FEE, "100.00", LocalDate.of(2025, 1, 1)),
                        transaction(FinanceType.EXPENSE, FinanceCategory.FIELD_RENTAL, "30.00", LocalDate.of(2025, 6, 1))));

        FinanceSummaryResponse summary = financeTransactionService.getSummary(null, null);

        assertThat(summary.getTotalIncome()).isEqualByComparingTo("100.00");
        assertThat(summary.getTotalExpense()).isEqualByComparingTo("30.00");
        assertThat(summary.getNetBalance()).isEqualByComparingTo("70.00");
        assertThat(summary.getByCategory().get(FinanceCategory.MONTHLY_FEE)).isEqualByComparingTo("100.00");
        assertThat(summary.getByCategory().get(FinanceCategory.FIELD_RENTAL)).isEqualByComparingTo("30.00");
    }

    @Test
    void getSummary_dateRange_excludesTransactionsOutsideRange() {
        when(financeTransactionRepository.findAll())
                .thenReturn(List.of(
                        transaction(FinanceType.INCOME, FinanceCategory.MONTHLY_FEE, "100.00", LocalDate.of(2025, 1, 1)),
                        transaction(FinanceType.INCOME, FinanceCategory.MONTHLY_FEE, "200.00", LocalDate.of(2025, 6, 15)),
                        transaction(FinanceType.INCOME, FinanceCategory.MONTHLY_FEE, "300.00", LocalDate.of(2025, 12, 31))));

        FinanceSummaryResponse summary =
                financeTransactionService.getSummary(LocalDate.of(2025, 6, 1), LocalDate.of(2025, 6, 30));

        assertThat(summary.getTotalIncome()).isEqualByComparingTo("200.00");
    }

    @Test
    void getSummary_dateRangeBoundaries_areInclusive() {
        LocalDate boundaryDate = LocalDate.of(2025, 6, 1);
        when(financeTransactionRepository.findAll())
                .thenReturn(List.of(transaction(FinanceType.INCOME, FinanceCategory.MONTHLY_FEE, "100.00", boundaryDate)));

        FinanceSummaryResponse summary = financeTransactionService.getSummary(boundaryDate, boundaryDate);

        assertThat(summary.getTotalIncome()).isEqualByComparingTo("100.00");
    }

    @Test
    void recordActivityFinanceEntry_activityNotFound_throwsResourceNotFoundException() {
        when(activityRepository.existsById(999L)).thenReturn(false);

        ActivityFinanceRequest request = ActivityFinanceRequest.builder()
                .activityId(999L)
                .financeType(FinanceType.EXPENSE)
                .financeCategory(FinanceCategory.TOURNAMENT_REGISTRATION)
                .amount(new BigDecimal("150.00"))
                .transactionDate(LocalDate.now())
                .build();

        assertThatThrownBy(() -> financeTransactionService.recordActivityFinanceEntry(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void recordActivityFinanceEntry_activityExists_recordsLinkedEntryAgainstActivity() {
        when(activityRepository.existsById(10L)).thenReturn(true);
        when(financeTransactionRepository.save(org.mockito.ArgumentMatchers.any()))
                .thenAnswer(inv -> inv.getArgument(0));
        when(financeTransactionMapper.toResponse(org.mockito.ArgumentMatchers.any()))
                .thenReturn(FinanceTransactionResponse.builder().build());

        ActivityFinanceRequest request = ActivityFinanceRequest.builder()
                .activityId(10L)
                .financeType(FinanceType.INCOME)
                .financeCategory(FinanceCategory.PRIZE_MONEY)
                .amount(new BigDecimal("500.00"))
                .transactionDate(LocalDate.now())
                .build();

        financeTransactionService.recordActivityFinanceEntry(request);

        org.mockito.ArgumentCaptor<FinanceTransaction> captor =
                org.mockito.ArgumentCaptor.forClass(FinanceTransaction.class);
        org.mockito.Mockito.verify(financeTransactionRepository).save(captor.capture());
        FinanceTransaction saved = captor.getValue();
        assertThat(saved.getFinanceType()).isEqualTo(FinanceType.INCOME);
        assertThat(saved.getFinanceCategory()).isEqualTo(FinanceCategory.PRIZE_MONEY);
        assertThat(saved.getReferenceId()).isEqualTo(10L);
        assertThat(saved.getReferenceType()).isEqualTo(FinanceReferenceType.ACTIVITY);
    }
}
