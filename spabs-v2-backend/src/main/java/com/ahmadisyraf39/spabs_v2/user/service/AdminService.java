package com.ahmadisyraf39.spabs_v2.user.service;

import com.ahmadisyraf39.spabs_v2.common.exception.ResourceNotFoundException;
import com.ahmadisyraf39.spabs_v2.user.dto.request.AdminRequest;
import com.ahmadisyraf39.spabs_v2.user.dto.response.AdminResponse;
import com.ahmadisyraf39.spabs_v2.user.entity.Admin;
import com.ahmadisyraf39.spabs_v2.user.entity.User;
import com.ahmadisyraf39.spabs_v2.user.mapper.AdminMapper;
import com.ahmadisyraf39.spabs_v2.user.repository.AdminRepository;
import com.ahmadisyraf39.spabs_v2.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final AdminMapper adminMapper;

    public AdminResponse create(AdminRequest request) {
        Admin admin = adminMapper.toEntity(request);
        admin.setUser(findUserById(request.getUserId()));
        return adminMapper.toResponse(adminRepository.save(admin));
    }

    public AdminResponse getById(Long id) {
        return adminMapper.toResponse(findEntityById(id));
    }

    public List<AdminResponse> getAll() {
        return adminRepository.findAll().stream().map(adminMapper::toResponse).toList();
    }

    public AdminResponse update(Long id, AdminRequest request) {
        Admin admin = findEntityById(id);
        adminMapper.updateEntity(admin, request);
        admin.setUser(findUserById(request.getUserId()));
        return adminMapper.toResponse(adminRepository.save(admin));
    }

    public void delete(Long id) {
        adminRepository.delete(findEntityById(id));
    }

    private Admin findEntityById(Long id) {
        return adminRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found: " + id));
    }

    private User findUserById(Long userId) {
        return userRepository
                .findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }
}
