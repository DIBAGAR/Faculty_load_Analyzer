package com.example.facultyload.dto.faculty;

import com.example.facultyload.entity.Designation;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record FacultyCreateRequest(
        @NotBlank @Size(max = 200) String name,
        @Email @NotBlank String email,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotNull Long departmentId,
        @NotNull Designation designation,
        Set<Long> primaryCourseIds,
        Set<Long> additionalCourseIds
) {
}

