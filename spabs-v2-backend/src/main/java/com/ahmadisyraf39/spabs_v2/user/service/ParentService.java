package com.ahmadisyraf39.spabs_v2.user.service;

import com.ahmadisyraf39.spabs_v2.common.exception.ResourceNotFoundException;
import com.ahmadisyraf39.spabs_v2.security.UserPrincipal;
import com.ahmadisyraf39.spabs_v2.user.dto.request.ParentRequest;
import com.ahmadisyraf39.spabs_v2.user.dto.request.ParentSelfUpdateRequest;
import com.ahmadisyraf39.spabs_v2.user.dto.response.ParentResponse;
import com.ahmadisyraf39.spabs_v2.user.entity.Parent;
import com.ahmadisyraf39.spabs_v2.user.entity.User;
import com.ahmadisyraf39.spabs_v2.user.mapper.ParentMapper;
import com.ahmadisyraf39.spabs_v2.user.repository.ParentRepository;
import com.ahmadisyraf39.spabs_v2.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ParentService {

    private final ParentRepository parentRepository;
    private final UserRepository userRepository;
    private final ParentMapper parentMapper;

    public ParentResponse create(ParentRequest request) {
        Parent parent = parentMapper.toEntity(request);
        parent.setUser(findUserById(request.getUserId()));
        return parentMapper.toResponse(parentRepository.save(parent));
    }

    public ParentResponse getById(Long id) {
        return parentMapper.toResponse(findEntityById(id));
    }

    public List<ParentResponse> getAll() {
        return parentRepository.findAll().stream().map(parentMapper::toResponse).toList();
    }

    public ParentResponse getMyProfile(UserPrincipal caller) {
        return parentRepository
                .findByUserId(caller.getId())
                .map(parentMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("No parent profile for this account"));
    }

    public ParentResponse update(Long id, ParentRequest request) {
        Parent parent = findEntityById(id);
        parentMapper.updateEntity(parent, request);
        parent.setUser(findUserById(request.getUserId()));
        return parentMapper.toResponse(parentRepository.save(parent));
    }

    public void delete(Long id) {
        parentRepository.delete(findEntityById(id));
    }

    public ParentResponse updateMyProfile(UserPrincipal caller, ParentSelfUpdateRequest request) {
        Parent parent = parentRepository
                .findByUserId(caller.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No parent profile for this account"));
        parent.setEmergencyContact(request.getEmergencyContact());
        parent.setAddress(request.getAddress());
        return parentMapper.toResponse(parentRepository.save(parent));
    }

    private Parent findEntityById(Long id) {
        return parentRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found: " + id));
    }

    private User findUserById(Long userId) {
        return userRepository
                .findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }
}
