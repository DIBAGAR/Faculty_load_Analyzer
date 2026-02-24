package com.example.facultyload.dto.course;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CourseCreateRequest(
        @NotBlank @Size(max = 50) String courseCode,
        @NotBlank @Size(max = 200) String name,
        @NotNull Integer credit,
        Set<Long> departmentIds
) {
}

