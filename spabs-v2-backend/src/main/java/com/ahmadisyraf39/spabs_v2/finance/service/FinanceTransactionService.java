package com.ahmadisyraf39.spabs_v2.finance.service;

import com.ahmadisyraf39.spabs_v2.activity.repository.ActivityRepository;
import com.ahmadisyraf39.spabs_v2.common.exception.ResourceNotFoundException;
import com.ahmadisyraf39.spabs_v2.finance.dto.request.ActivityFinanceRequest;
import com.ahmadisyraf39.spabs_v2.finance.dto.request.FinanceTransactionRequest;
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
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FinanceTransactionService {

    private final FinanceTransactionRepository financeTransactionRepository;
    private final FinanceTransactionMapper financeTransactionMapper;
    private final ActivityRepository activityRepository;

    public FinanceTransactionResponse create(FinanceTransactionRequest request) {
        FinanceTransaction transaction = financeTransactionMapper.toEntity(request);
        return financeTransactionMapper.toResponse(financeTransactionRepository.save(transaction));
    }

    public FinanceTransactionResponse getById(Long id) {
        return financeTransactionMapper.toResponse(findEntityById(id));
    }

    public List<FinanceTransactionResponse> getAll() {
        return financeTransactionRepository.findAll().stream()
                .map(financeTransactionMapper::toResponse)
                .toList();
    }

    public FinanceTransactionResponse update(Long id, FinanceTransactionRequest request) {
        FinanceTransaction transaction = findEntityById(id);
        financeTransactionMapper.updateEntity(transaction, request);
        return financeTransactionMapper.toResponse(financeTransactionRepository.save(transaction));
    }

    public void delete(Long id) {
        financeTransactionRepository.delete(findEntityById(id));
    }

    public FinanceSummaryResponse getSummary(LocalDate startDate, LocalDate endDate) {
        List<FinanceTransaction> transactions = financeTransactionRepository.findAll().stream()
                .filter(t -> startDate == null || !t.getTransactionDate().isBefore(startDate))
                .filter(t -> endDate == null || !t.getTransactionDate().isAfter(endDate))
                .toList();

        BigDecimal totalIncome = sumByType(transactions, FinanceType.INCOME);
        BigDecimal totalExpense = sumByType(transactions, FinanceType.EXPENSE);

        Map<FinanceCategory, BigDecimal> byCategory = transactions.stream()
                .collect(Collectors.groupingBy(
                        FinanceTransaction::getFinanceCategory,
                        Collectors.reducing(BigDecimal.ZERO, FinanceTransaction::getAmount, BigDecimal::add)));

        return FinanceSummaryResponse.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .netBalance(totalIncome.subtract(totalExpense))
                .byCategory(byCategory)
                .build();
    }

    private BigDecimal sumByType(List<FinanceTransaction> transactions, FinanceType type) {
        return transactions.stream()
                .filter(t -> t.getFinanceType() == type)
                .map(FinanceTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public FinanceTransaction recordLinked(
            FinanceType financeType,
            FinanceCategory financeCategory,
            BigDecimal amount,
            String description,
            LocalDate transactionDate,
            Long referenceId,
            FinanceReferenceType referenceType) {
        FinanceTransaction transaction = FinanceTransaction.builder()
                .financeType(financeType)
                .financeCategory(financeCategory)
                .amount(amount)
                .description(description)
                .transactionDate(transactionDate)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .build();
        return financeTransactionRepository.save(transaction);
    }

    public FinanceTransactionResponse recordActivityFinanceEntry(ActivityFinanceRequest request) {
        if (!activityRepository.existsById(request.getActivityId())) {
            throw new ResourceNotFoundException("Activity not found: " + request.getActivityId());
        }
        FinanceTransaction transaction = recordLinked(
                request.getFinanceType(),
                request.getFinanceCategory(),
                request.getAmount(),
                request.getDescription(),
                request.getTransactionDate(),
                request.getActivityId(),
                FinanceReferenceType.ACTIVITY);
        return financeTransactionMapper.toResponse(transaction);
    }

    private FinanceTransaction findEntityById(Long id) {
        return financeTransactionRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FinanceTransaction not found: " + id));
    }
}
