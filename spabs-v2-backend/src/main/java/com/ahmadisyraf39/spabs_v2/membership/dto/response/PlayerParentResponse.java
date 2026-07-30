package com.ahmadisyraf39.spabs_v2.membership.dto.response;

import com.ahmadisyraf39.spabs_v2.membership.entity.enums.ParentRelationship;
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
public class PlayerParentResponse {

    private Long id;
    private Long playerId;
    private Long parentId;
    private ParentRelationship relationship;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
