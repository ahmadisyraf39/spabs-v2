package com.ahmadisyraf39.spabs_v2.membership.service;

import com.ahmadisyraf39.spabs_v2.common.exception.ResourceNotFoundException;
import com.ahmadisyraf39.spabs_v2.membership.dto.request.CoachMembershipRequest;
import com.ahmadisyraf39.spabs_v2.membership.dto.response.CoachMembershipResponse;
import com.ahmadisyraf39.spabs_v2.membership.entity.CoachMembership;
import com.ahmadisyraf39.spabs_v2.membership.mapper.CoachMembershipMapper;
import com.ahmadisyraf39.spabs_v2.membership.repository.CoachMembershipRepository;
import com.ahmadisyraf39.spabs_v2.user.entity.Coach;
import com.ahmadisyraf39.spabs_v2.user.repository.CoachRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CoachMembershipService {

    private final CoachMembershipRepository coachMembershipRepository;
    private final CoachRepository coachRepository;
    private final CoachMembershipMapper coachMembershipMapper;

    public CoachMembershipResponse create(CoachMembershipRequest request) {
        CoachMembership coachMembership = coachMembershipMapper.toEntity(request);
        coachMembership.setCoach(findCoachById(request.getCoachId()));
        return coachMembershipMapper.toResponse(coachMembershipRepository.save(coachMembership));
    }

    public CoachMembershipResponse getById(Long id) {
        return coachMembershipMapper.toResponse(findEntityById(id));
    }

    public List<CoachMembershipResponse> getAll() {
        return coachMembershipRepository.findAll().stream().map(coachMembershipMapper::toResponse).toList();
    }

    public List<CoachMembershipResponse> getByCoach(Long coachId) {
        return coachMembershipRepository.findByCoachId(coachId).stream()
                .map(coachMembershipMapper::toResponse)
                .toList();
    }

    public CoachMembershipResponse update(Long id, CoachMembershipRequest request) {
        CoachMembership coachMembership = findEntityById(id);
        coachMembershipMapper.updateEntity(coachMembership, request);
        coachMembership.setCoach(findCoachById(request.getCoachId()));
        return coachMembershipMapper.toResponse(coachMembershipRepository.save(coachMembership));
    }

    public void delete(Long id) {
        coachMembershipRepository.delete(findEntityById(id));
    }

    private CoachMembership findEntityById(Long id) {
        return coachMembershipRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CoachMembership not found: " + id));
    }

    private Coach findCoachById(Long coachId) {
        return coachRepository
                .findById(coachId)
                .orElseThrow(() -> new ResourceNotFoundException("Coach not found: " + coachId));
    }
}
