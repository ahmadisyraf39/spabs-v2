package com.ahmadisyraf39.spabs_v2.sponsorship.repository;

import com.ahmadisyraf39.spabs_v2.sponsorship.entity.ClubSponsorship;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubSponsorshipRepository extends JpaRepository<ClubSponsorship, Long> {

    List<ClubSponsorship> findBySponsorId(Long sponsorId);
}
