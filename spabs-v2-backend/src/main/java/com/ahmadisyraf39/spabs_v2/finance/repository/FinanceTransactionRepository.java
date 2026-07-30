package com.ahmadisyraf39.spabs_v2.finance.repository;

import com.ahmadisyraf39.spabs_v2.finance.entity.FinanceTransaction;
import com.ahmadisyraf39.spabs_v2.finance.entity.enums.FinanceType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FinanceTransactionRepository extends JpaRepository<FinanceTransaction, Long> {

    List<FinanceTransaction> findByFinanceType(FinanceType financeType);
}
