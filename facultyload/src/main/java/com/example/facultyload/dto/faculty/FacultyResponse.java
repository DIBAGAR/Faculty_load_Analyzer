package com.example.facultyload.dto.faculty;

import com.example.facultyload.entity.Designation;

import java.util.Set;

public record FacultyResponse(
        Long id,
        String name,
        String email,
        Long departmentId,
        Designation designation,
        boolean isHod,
        Integer monthlyAssignedHours,
        Integer totalAssignedHours,
        Set<Long> primaryCourseIds,
        Set<Long> additionalCourseIds
) {
}

