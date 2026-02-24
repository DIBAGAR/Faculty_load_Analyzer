package com.example.facultyload.dto.timetable;

import java.util.List;

public record TimetableResponse(
        Long id,
        Long departmentId,
        Integer yearOfStudy,
        Integer semester,
        String section,
        Integer versionNo,
        boolean isActive,
        List<TimetableEntryResponse> entries
) {
}

