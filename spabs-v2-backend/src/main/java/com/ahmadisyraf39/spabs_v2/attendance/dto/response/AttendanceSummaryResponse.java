package com.ahmadisyraf39.spabs_v2.attendance.dto.response;

import java.util.List;
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
public class AttendanceSummaryResponse {

    private Long playerId;
    private Long teamId;
    private long totalRecords;
    private long presentCount;
    private long lateCount;
    private long absentCount;
    private long excusedCount;
    private double attendancePercentage;
    private List<AttendanceResponse> records;
}
