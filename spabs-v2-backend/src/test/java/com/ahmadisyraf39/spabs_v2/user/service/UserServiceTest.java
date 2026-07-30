package com.ahmadisyraf39.spabs_v2.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ahmadisyraf39.spabs_v2.common.email.EmailService;
import com.ahmadisyraf39.spabs_v2.common.exception.InvalidPasswordException;
import com.ahmadisyraf39.spabs_v2.user.dto.request.ResetPasswordRequest;
import com.ahmadisyraf39.spabs_v2.user.dto.request.UserRequest;
import com.ahmadisyraf39.spabs_v2.user.dto.response.ResetPasswordResponse;
import com.ahmadisyraf39.spabs_v2.user.dto.response.UserResponse;
import com.ahmadisyraf39.spabs_v2.user.entity.User;
import com.ahmadisyraf39.spabs_v2.user.entity.enums.UserRole;
import com.ahmadisyraf39.spabs_v2.user.mapper.UserMapper;
import com.ahmadisyraf39.spabs_v2.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserService userService;

    @Test
    void create_blankPassword_generatesOneAndEmailsIt() {
        User entity = new User();
        when(userMapper.toEntity(any())).thenReturn(entity);
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(userRepository.save(entity)).thenReturn(entity);
        when(userMapper.toResponse(entity)).thenReturn(UserResponse.builder().build());

        UserRequest request = UserRequest.builder()
                .email("new@spabs.example")
                .fullName("New User")
                .role(UserRole.PARENT)
                .build();

        userService.create(request);

        assertThat(entity.getPassword()).isEqualTo("hashed");
        assertThat(entity.isMustChangePassword()).isTrue();
        ArgumentCaptor<String> passwordCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendWelcomeEmail(eq(entity), passwordCaptor.capture());
        assertThat(passwordCaptor.getValue()).hasSize(12);
    }

    @Test
    void create_suppliedPassword_usesItAndEmailsTheSameValue() {
        User entity = new User();
        when(userMapper.toEntity(any())).thenReturn(entity);
        when(passwordEncoder.encode("MySuppliedPass1")).thenReturn("hashed-supplied");
        when(userRepository.save(entity)).thenReturn(entity);
        when(userMapper.toResponse(entity)).thenReturn(UserResponse.builder().build());

        UserRequest request = UserRequest.builder()
                .email("new@spabs.example")
                .fullName("New User")
                .role(UserRole.PARENT)
                .password("MySuppliedPass1")
                .build();

        userService.create(request);

        assertThat(entity.getPassword()).isEqualTo("hashed-supplied");
        verify(emailService).sendWelcomeEmail(entity, "MySuppliedPass1");
    }

    @Test
    void create_activeOmitted_defaultsToTrue() {
        User entity = new User();
        when(userMapper.toEntity(any())).thenReturn(entity);
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(userRepository.save(entity)).thenReturn(entity);
        when(userMapper.toResponse(entity)).thenReturn(UserResponse.builder().build());

        UserRequest request = UserRequest.builder()
                .email("new@spabs.example")
                .fullName("New User")
                .role(UserRole.PARENT)
                .build();

        userService.create(request);

        assertThat(entity.isActive()).isTrue();
    }

    @Test
    void update_blankPassword_leavesExistingPasswordUntouched() {
        User existing = new User();
        existing.setPassword("original-hash");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);
        when(userMapper.toResponse(existing)).thenReturn(UserResponse.builder().build());

        UserRequest request = UserRequest.builder()
                .email("user@spabs.example")
                .fullName("User")
                .role(UserRole.PARENT)
                .phoneNumber("012-3456789")
                .build();

        userService.update(1L, request);

        assertThat(existing.getPassword()).isEqualTo("original-hash");
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void update_suppliedPassword_rehashesIt() {
        User existing = new User();
        existing.setPassword("original-hash");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("NewPassword1")).thenReturn("new-hash");
        when(userRepository.save(existing)).thenReturn(existing);
        when(userMapper.toResponse(existing)).thenReturn(UserResponse.builder().build());

        UserRequest request = UserRequest.builder()
                .email("user@spabs.example")
                .fullName("User")
                .role(UserRole.PARENT)
                .password("NewPassword1")
                .build();

        userService.update(1L, request);

        assertThat(existing.getPassword()).isEqualTo("new-hash");
    }

    @Test
    void changePassword_wrongCurrentPassword_throwsInvalidPasswordException() {
        User existing = new User();
        existing.setPassword("hashed");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword(1L, "wrong", "newPass123"))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    void changePassword_correctCurrentPassword_updatesAndClearsMustChangeFlag() {
        User existing = new User();
        existing.setPassword("hashed");
        existing.setMustChangePassword(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("correct", "hashed")).thenReturn(true);
        when(passwordEncoder.encode("newPass123")).thenReturn("new-hashed");

        userService.changePassword(1L, "correct", "newPass123");

        assertThat(existing.getPassword()).isEqualTo("new-hashed");
        assertThat(existing.isMustChangePassword()).isFalse();
    }

    @Test
    void resetPassword_blankNewPassword_generatesOne() {
        User existing = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode(any())).thenReturn("hashed");

        ResetPasswordResponse response = userService.resetPassword(1L, new ResetPasswordRequest());

        assertThat(response.getTemporaryPassword()).hasSize(12);
        assertThat(existing.isMustChangePassword()).isTrue();
    }

    @Test
    void resetPassword_suppliedNewPassword_usesIt() {
        User existing = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("AdminChosenPass1")).thenReturn("hashed-admin-chosen");

        ResetPasswordResponse response = userService.resetPassword(
                1L, ResetPasswordRequest.builder().newPassword("AdminChosenPass1").build());

        assertThat(response.getTemporaryPassword()).isEqualTo("AdminChosenPass1");
        assertThat(existing.getPassword()).isEqualTo("hashed-admin-chosen");
    }
}
