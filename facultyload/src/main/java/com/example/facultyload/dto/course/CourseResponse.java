package com.example.facultyload.dto.course;

import java.util.Set;

public record CourseResponse(
        Long id,
        String courseCode,
        String name,
        Integer credit,
        Set<Long> departmentIds
) {
}

