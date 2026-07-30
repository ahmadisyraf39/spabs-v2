package com.ahmadisyraf39.spabs_v2.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ahmadisyraf39.spabs_v2.auth.entity.PasswordResetToken;
import com.ahmadisyraf39.spabs_v2.auth.repository.PasswordResetTokenRepository;
import com.ahmadisyraf39.spabs_v2.common.email.EmailService;
import com.ahmadisyraf39.spabs_v2.common.exception.InvalidRequestException;
import com.ahmadisyraf39.spabs_v2.user.entity.User;
import com.ahmadisyraf39.spabs_v2.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private PasswordResetService passwordResetService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(passwordResetService, "tokenExpiryMinutes", 30L);
    }

    @Test
    void requestReset_existingEmail_deletesOldTokenCreatesNewOneAndEmails() {
        User user = User.builder().id(1L).email("user@spabs.example").fullName("Test User").build();
        when(userRepository.findByEmail("user@spabs.example")).thenReturn(Optional.of(user));

        passwordResetService.requestReset("user@spabs.example");

        verify(passwordResetTokenRepository).deleteByUserId(1L);
        ArgumentCaptor<PasswordResetToken> captor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(passwordResetTokenRepository).save(captor.capture());
        PasswordResetToken saved = captor.getValue();
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getToken()).isNotBlank();
        assertThat(saved.getExpiresAt()).isAfter(LocalDateTime.now());
        verify(emailService).sendPasswordResetEmail(eq(user), eq(saved.getToken()));
    }

    @Test
    void requestReset_unknownEmail_doesNothingAndSendsNoEmail() {
        when(userRepository.findByEmail("nobody@spabs.example")).thenReturn(Optional.empty());

        passwordResetService.requestReset("nobody@spabs.example");

        verify(passwordResetTokenRepository, never()).deleteByUserId(anyLong());
        verify(passwordResetTokenRepository, never()).save(any());
        verify(emailService, never()).sendPasswordResetEmail(any(), any());
    }

    @Test
    void resetPassword_validToken_updatesPasswordClearsMustChangeAndDeletesToken() {
        User user = User.builder()
                .id(1L)
                .email("user@spabs.example")
                .fullName("Test User")
                .mustChangePassword(true)
                .build();
        PasswordResetToken token = PasswordResetToken.builder()
                .user(user)
                .token("valid-token")
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();
        when(passwordResetTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("newPass123")).thenReturn("hashed-password");

        passwordResetService.resetPassword("valid-token", "newPass123");

        assertThat(user.getPassword()).isEqualTo("hashed-password");
        assertThat(user.isMustChangePassword()).isFalse();
        verify(userRepository).save(user);
        verify(passwordResetTokenRepository).delete(token);
    }

    @Test
    void resetPassword_unknownToken_throwsInvalidRequestException() {
        when(passwordResetTokenRepository.findByToken("bad-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> passwordResetService.resetPassword("bad-token", "newPass123"))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void resetPassword_expiredToken_throwsAndDeletesTokenWithoutTouchingUser() {
        User user = User.builder().id(1L).build();
        PasswordResetToken token = PasswordResetToken.builder()
                .user(user)
                .token("expired-token")
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .build();
        when(passwordResetTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> passwordResetService.resetPassword("expired-token", "newPass123"))
                .isInstanceOf(InvalidRequestException.class);

        verify(passwordResetTokenRepository).delete(token);
        verify(userRepository, never()).save(any());
    }
}
