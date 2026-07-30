package com.ahmadisyraf39.spabs_v2.attendance.dto.request;

import com.ahmadisyraf39.spabs_v2.attendance.entity.enums.AttendanceStatus;
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
public class BulkAttendanceEntry {

    @NotNull
    private Long playerId;

    @NotNull
    private AttendanceStatus status;

    private String notes;
}
