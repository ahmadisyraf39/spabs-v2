package com.ahmadisyraf39.spabs_v2.user.dto.request;

import com.ahmadisyraf39.spabs_v2.user.entity.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {

    @NotBlank
    @Email
    private String email;

    @Size(min = 8, message = "password must be at least 8 characters")
    private String password;

    @NotBlank
    private String fullName;

    private String phoneNumber;

    @NotNull
    private UserRole role;

    private Boolean active;
}
