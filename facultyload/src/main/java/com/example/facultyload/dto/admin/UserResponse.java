package com.example.facultyload.dto.admin;

import com.example.facultyload.entity.RoleName;

public record UserResponse(
        Long id,
        String email,
        RoleName role,
        Boolean isActive
) {
}

