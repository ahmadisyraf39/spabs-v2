package com.ahmadisyraf39.spabs_v2.activity.repository;

import com.ahmadisyraf39.spabs_v2.activity.entity.Activity;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    List<Activity> findByTeamId(Long teamId);

    List<Activity> findByTeamIdInAndStartAtAfter(List<Long> teamIds, LocalDateTime startAt);
}
