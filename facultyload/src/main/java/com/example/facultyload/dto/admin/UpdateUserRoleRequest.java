package com.example.facultyload.dto.admin;

import com.example.facultyload.entity.RoleName;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequest(
        @NotNull RoleName role
) {
}

