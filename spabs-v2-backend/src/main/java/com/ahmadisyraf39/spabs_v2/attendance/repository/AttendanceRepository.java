package com.ahmadisyraf39.spabs_v2.attendance.repository;

import com.ahmadisyraf39.spabs_v2.attendance.entity.Attendance;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByActivityId(Long activityId);

    List<Attendance> findByPlayerId(Long playerId);

    Optional<Attendance> findByActivityIdAndPlayerId(Long activityId, Long playerId);
}
