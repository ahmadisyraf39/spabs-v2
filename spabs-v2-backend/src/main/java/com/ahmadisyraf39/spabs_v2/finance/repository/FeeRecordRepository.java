package com.ahmadisyraf39.spabs_v2.finance.repository;

import com.ahmadisyraf39.spabs_v2.finance.entity.FeeRecord;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeeRecordRepository extends JpaRepository<FeeRecord, Long> {

    List<FeeRecord> findByPlayerId(Long playerId);

    List<FeeRecord> findByTeamId(Long teamId);
}
