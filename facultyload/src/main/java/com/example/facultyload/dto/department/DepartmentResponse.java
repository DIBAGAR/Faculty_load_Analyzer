package com.example.facultyload.dto.department;

public record DepartmentResponse(
        Long id,
        String name,
        String code,
        Long hodFacultyId,
        Long tempHodFacultyId
) {
}

