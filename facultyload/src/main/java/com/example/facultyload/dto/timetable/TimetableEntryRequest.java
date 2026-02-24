package com.example.facultyload.dto.timetable;

import com.example.facultyload.entity.SessionType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;

public record TimetableEntryRequest(
        @NotNull DayOfWeek dayOfWeek,
        @NotNull @Min(1) @Max(7) Integer hourNumber,
        @NotNull Long courseId,
        @NotNull Long venueId,
        @NotNull SessionType type,
        Long defaultFacultyId
) {
}

