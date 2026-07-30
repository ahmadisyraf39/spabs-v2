package com.ahmadisyraf39.spabs_v2.activity.dto.request;

import com.ahmadisyraf39.spabs_v2.activity.entity.enums.ActivityType;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
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
public class RecurringActivityRequest {

    private List<Long> teamIds;

    private Boolean applyToAllTeams;

    @NotNull
    private ActivityType type;

    private String title;

    private String location;

    private String description;

    @NotNull
    private DayOfWeek dayOfWeek;

    @NotNull
    private LocalTime startTime;

    private LocalTime endTime;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;
}
