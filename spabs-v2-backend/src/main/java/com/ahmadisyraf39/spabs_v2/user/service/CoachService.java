package com.ahmadisyraf39.spabs_v2.user.service;

import com.ahmadisyraf39.spabs_v2.common.exception.ResourceNotFoundException;
import com.ahmadisyraf39.spabs_v2.security.UserPrincipal;
import com.ahmadisyraf39.spabs_v2.user.dto.request.CoachRequest;
import com.ahmadisyraf39.spabs_v2.user.dto.response.CoachResponse;
import com.ahmadisyraf39.spabs_v2.user.entity.Coach;
import com.ahmadisyraf39.spabs_v2.user.entity.User;
import com.ahmadisyraf39.spabs_v2.user.mapper.CoachMapper;
import com.ahmadisyraf39.spabs_v2.user.repository.CoachRepository;
import com.ahmadisyraf39.spabs_v2.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CoachService {

    private final CoachRepository coachRepository;
    private final UserRepository userRepository;
    private final CoachMapper coachMapper;

    public CoachResponse create(CoachRequest request) {
        Coach coach = coachMapper.toEntity(request);
        coach.setUser(findUserById(request.getUserId()));
        return coachMapper.toResponse(coachRepository.save(coach));
    }

    public CoachResponse getById(Long id) {
        return coachMapper.toResponse(findEntityById(id));
    }

    public List<CoachResponse> getAll() {
        return coachRepository.findAll().stream().map(coachMapper::toResponse).toList();
    }

    public CoachResponse getMyProfile(UserPrincipal caller) {
        return coachRepository
                .findByUserId(caller.getId())
                .map(coachMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("No coach profile for this account"));
    }

    public CoachResponse update(Long id, CoachRequest request) {
        Coach coach = findEntityById(id);
        coachMapper.updateEntity(coach, request);
        coach.setUser(findUserById(request.getUserId()));
        return coachMapper.toResponse(coachRepository.save(coach));
    }

    public void delete(Long id) {
        coachRepository.delete(findEntityById(id));
    }

    private Coach findEntityById(Long id) {
        return coachRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coach not found: " + id));
    }

    private User findUserById(Long userId) {
        return userRepository
                .findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }
}
