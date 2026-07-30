package com.ahmadisyraf39.spabs_v2.user.service;

import com.ahmadisyraf39.spabs_v2.common.email.EmailService;
import com.ahmadisyraf39.spabs_v2.common.exception.InvalidPasswordException;
import com.ahmadisyraf39.spabs_v2.common.exception.ResourceNotFoundException;
import com.ahmadisyraf39.spabs_v2.security.UserPrincipal;
import com.ahmadisyraf39.spabs_v2.user.dto.request.ResetPasswordRequest;
import com.ahmadisyraf39.spabs_v2.user.dto.request.UserRequest;
import com.ahmadisyraf39.spabs_v2.user.dto.request.UserSelfUpdateRequest;
import com.ahmadisyraf39.spabs_v2.user.dto.response.ResetPasswordResponse;
import com.ahmadisyraf39.spabs_v2.user.dto.response.UserResponse;
import com.ahmadisyraf39.spabs_v2.user.entity.User;
import com.ahmadisyraf39.spabs_v2.user.mapper.UserMapper;
import com.ahmadisyraf39.spabs_v2.user.repository.UserRepository;
import java.security.SecureRandom;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserResponse create(UserRequest request) {
        User user = userMapper.toEntity(request);
        String password = isBlank(request.getPassword()) ? generateTemporaryPassword() : request.getPassword();
        user.setPassword(passwordEncoder.encode(password));
        user.setMustChangePassword(true);
        user.setActive(request.getActive() == null || request.getActive());
        User saved = userRepository.save(user);
        emailService.sendWelcomeEmail(saved, password);
        return userMapper.toResponse(saved);
    }

    public UserResponse getById(Long id) {
        return userMapper.toResponse(findEntityById(id));
    }

    public List<UserResponse> getAll() {
        return userRepository.findAll().stream().map(userMapper::toResponse).toList();
    }

    public UserResponse update(Long id, UserRequest request) {
        User user = findEntityById(id);
        userMapper.updateEntity(user, request);
        if (!isBlank(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        user.setActive(request.getActive() == null || request.getActive());
        return userMapper.toResponse(userRepository.save(user));
    }

    public void delete(Long id) {
        userRepository.delete(findEntityById(id));
    }

    public UserResponse updateMyProfile(UserPrincipal caller, UserSelfUpdateRequest request) {
        User user = findEntityById(caller.getId());
        user.setPhoneNumber(request.getPhoneNumber());
        return userMapper.toResponse(userRepository.save(user));
    }

    public void changePassword(Long id, String currentPassword, String newPassword) {
        User user = findEntityById(id);
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new InvalidPasswordException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false);
        userRepository.save(user);
    }

    public ResetPasswordResponse resetPassword(Long id, ResetPasswordRequest request) {
        User user = findEntityById(id);
        String newPassword = isBlank(request.getNewPassword()) ? generateTemporaryPassword() : request.getNewPassword();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(true);
        userRepository.save(user);
        return ResetPasswordResponse.builder()
                .userId(id)
                .temporaryPassword(newPassword)
                .build();
    }

    private static final String TEMP_PASSWORD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private String generateTemporaryPassword() {
        StringBuilder password = new StringBuilder(12);
        for (int i = 0; i < 12; i++) {
            password.append(TEMP_PASSWORD_CHARS.charAt(SECURE_RANDOM.nextInt(TEMP_PASSWORD_CHARS.length())));
        }
        return password.toString();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private User findEntityById(Long id) {
        return userRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }
}
