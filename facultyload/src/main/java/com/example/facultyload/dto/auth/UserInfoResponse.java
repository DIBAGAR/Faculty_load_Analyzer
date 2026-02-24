package com.example.facultyload.dto.auth;

import com.example.facultyload.entity.RoleName;

public record UserInfoResponse(
        Long id,
        String email,
        RoleName role,
        Long facultyId,
        Long departmentId
) {
}

