package com.ahmadisyraf39.spabs_v2.user.dto.response;

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
public class ParentResponse {

    private Long id;
    private Long userId;
    private String emergencyContact;
    private String address;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
