package com.example.facultyload.dto.timetable;

import com.example.facultyload.entity.SessionType;

import java.time.DayOfWeek;

public record TimetableEntryResponse(
        Long id,
        DayOfWeek dayOfWeek,
        Integer hourNumber,
        Long courseId,
        Long venueId,
        SessionType type,
        Long defaultFacultyId
) {
}

