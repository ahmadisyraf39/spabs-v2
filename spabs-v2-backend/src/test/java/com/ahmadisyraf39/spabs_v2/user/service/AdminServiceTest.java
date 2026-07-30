package com.ahmadisyraf39.spabs_v2.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.ahmadisyraf39.spabs_v2.common.exception.ResourceNotFoundException;
import com.ahmadisyraf39.spabs_v2.user.dto.request.AdminRequest;
import com.ahmadisyraf39.spabs_v2.user.dto.response.AdminResponse;
import com.ahmadisyraf39.spabs_v2.user.entity.Admin;
import com.ahmadisyraf39.spabs_v2.user.entity.User;
import com.ahmadisyraf39.spabs_v2.user.mapper.AdminMapper;
import com.ahmadisyraf39.spabs_v2.user.repository.AdminRepository;
import com.ahmadisyraf39.spabs_v2.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AdminMapper adminMapper;

    @InjectMocks
    private AdminService adminService;

    @Test
    void create_resolvesUserAndSaves() {
        User user = User.builder().id(1L).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Admin entity = new Admin();
        when(adminMapper.toEntity(org.mockito.ArgumentMatchers.any())).thenReturn(entity);
        when(adminRepository.save(entity)).thenReturn(entity);
        when(adminMapper.toResponse(entity)).thenReturn(AdminResponse.builder().build());

        adminService.create(AdminRequest.builder().userId(1L).build());

        assertThat(entity.getUser()).isEqualTo(user);
    }

    @Test
    void create_userNotFound_throwsResourceNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.create(AdminRequest.builder().userId(1L).build()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getById_notFound_throwsResourceNotFoundException() {
        when(adminRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.getById(1L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
