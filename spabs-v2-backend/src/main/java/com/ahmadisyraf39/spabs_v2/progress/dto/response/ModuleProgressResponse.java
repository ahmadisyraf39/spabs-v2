package com.ahmadisyraf39.spabs_v2.progress.dto.response;

import com.ahmadisyraf39.spabs_v2.progress.entity.enums.ModuleProgressStatus;
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
public class ModuleProgressResponse {

    private Long moduleId;
    private String moduleName;
    private ModuleProgressStatus status;
    private int percentage;
}
