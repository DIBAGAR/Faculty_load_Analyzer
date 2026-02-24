package com.example.facultyload.dto.department;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepartmentCreateRequest(
        @NotBlank @Size(max = 200) String name,
        @Size(max = 50) String code
) {
}

