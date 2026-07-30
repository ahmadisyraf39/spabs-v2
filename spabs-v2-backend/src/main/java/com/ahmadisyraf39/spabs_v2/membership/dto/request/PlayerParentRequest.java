package com.ahmadisyraf39.spabs_v2.membership.dto.request;

import com.ahmadisyraf39.spabs_v2.membership.entity.enums.ParentRelationship;
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
public class PlayerParentRequest {

    @NotNull
    private Long playerId;

    @NotNull
    private Long parentId;

    @NotNull
    private ParentRelationship relationship;
}
