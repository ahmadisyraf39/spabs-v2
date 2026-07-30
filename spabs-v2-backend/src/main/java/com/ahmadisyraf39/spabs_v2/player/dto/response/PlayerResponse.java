package com.ahmadisyraf39.spabs_v2.player.dto.response;

import com.ahmadisyraf39.spabs_v2.player.entity.enums.Gender;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
public class PlayerResponse {

    private Long id;
    private String fullName;
    private LocalDate dateOfBirth;
    private Gender gender;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
