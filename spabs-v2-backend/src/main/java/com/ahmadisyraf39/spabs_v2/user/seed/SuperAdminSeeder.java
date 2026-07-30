package com.ahmadisyraf39.spabs_v2.user.seed;

import com.ahmadisyraf39.spabs_v2.user.dto.request.AdminRequest;
import com.ahmadisyraf39.spabs_v2.user.dto.request.UserRequest;
import com.ahmadisyraf39.spabs_v2.user.dto.response.UserResponse;
import com.ahmadisyraf39.spabs_v2.user.entity.enums.UserRole;
import com.ahmadisyraf39.spabs_v2.user.repository.UserRepository;
import com.ahmadisyraf39.spabs_v2.user.service.AdminService;
import com.ahmadisyraf39.spabs_v2.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SuperAdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserService userService;
    private final AdminService adminService;

    @Value("${app.seed.super-admin.email}")
    private String seedEmail;

    @Value("${app.seed.super-admin.password}")
    private String seedPassword;

    @Override
    public void run(String... args) {
        if (userRepository.existsByRole(UserRole.SUPER_ADMIN)) {
            return;
        }

        UserResponse user = userService.create(UserRequest.builder()
                .email(seedEmail)
                .password(seedPassword)
                .fullName("Super Admin")
                .role(UserRole.SUPER_ADMIN)
                .active(true)
                .build());

        adminService.create(AdminRequest.builder().userId(user.getId()).build());

        log.info("Seeded initial SUPER_ADMIN account: {}", seedEmail);
    }
}
