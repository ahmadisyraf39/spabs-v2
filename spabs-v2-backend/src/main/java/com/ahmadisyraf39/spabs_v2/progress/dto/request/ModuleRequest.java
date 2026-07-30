package com.ahmadisyraf39.spabs_v2.progress.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class ModuleRequest {

    @NotNull
    private Long skillId;

    @NotBlank
    private String name;

    private String criteria25;

    private String criteria50;

    private String criteria75;

    private String criteria100;
}
