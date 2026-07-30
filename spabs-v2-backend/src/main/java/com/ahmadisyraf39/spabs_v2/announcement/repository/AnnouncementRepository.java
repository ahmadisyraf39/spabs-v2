package com.ahmadisyraf39.spabs_v2.announcement.repository;

import com.ahmadisyraf39.spabs_v2.announcement.entity.Announcement;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    List<Announcement> findAllByOrderByCreatedAtDesc();

    List<Announcement> findByTeamId(Long teamId);

    List<Announcement> findByTeamIdIsNull();

    List<Announcement> findByTeamIdIn(List<Long> teamIds);
}
