package com.ahmadisyraf39.spabs_v2.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.ahmadisyraf39.spabs_v2.common.exception.ResourceNotFoundException;
import com.ahmadisyraf39.spabs_v2.security.UserPrincipal;
import com.ahmadisyraf39.spabs_v2.user.dto.request.ParentRequest;
import com.ahmadisyraf39.spabs_v2.user.dto.response.ParentResponse;
import com.ahmadisyraf39.spabs_v2.user.entity.Parent;
import com.ahmadisyraf39.spabs_v2.user.entity.User;
import com.ahmadisyraf39.spabs_v2.user.mapper.ParentMapper;
import com.ahmadisyraf39.spabs_v2.user.repository.ParentRepository;
import com.ahmadisyraf39.spabs_v2.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ParentServiceTest {

    @Mock
    private ParentRepository parentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ParentMapper parentMapper;

    @InjectMocks
    private ParentService parentService;

    @Test
    void create_resolvesUserAndSaves() {
        User user = User.builder().id(1L).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Parent entity = new Parent();
        when(parentMapper.toEntity(org.mockito.ArgumentMatchers.any())).thenReturn(entity);
        when(parentRepository.save(entity)).thenReturn(entity);
        when(parentMapper.toResponse(entity)).thenReturn(ParentResponse.builder().build());

        parentService.create(ParentRequest.builder().userId(1L).build());

        assertThat(entity.getUser()).isEqualTo(user);
    }

    @Test
    void create_userNotFound_throwsResourceNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> parentService.create(ParentRequest.builder().userId(1L).build()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getById_notFound_throwsResourceNotFoundException() {
        when(parentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> parentService.getById(1L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getMyProfile_found_returnsResponse() {
        User user = User.builder().id(7L).build();
        Parent parent = new Parent();
        when(parentRepository.findByUserId(7L)).thenReturn(Optional.of(parent));
        when(parentMapper.toResponse(parent)).thenReturn(ParentResponse.builder().id(3L).build());

        ParentResponse response = parentService.getMyProfile(new UserPrincipal(user));

        assertThat(response.getId()).isEqualTo(3L);
    }

    @Test
    void getMyProfile_noParentProfile_throwsResourceNotFoundException() {
        User user = User.builder().id(7L).build();
        when(parentRepository.findByUserId(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> parentService.getMyProfile(new UserPrincipal(user)))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
