package com.ahmadisyraf39.spabs_v2.progress.dto.response;

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
public class ModuleResponse {

    private Long id;
    private Long skillId;
    private String name;
    private String criteria25;
    private String criteria50;
    private String criteria75;
    private String criteria100;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
