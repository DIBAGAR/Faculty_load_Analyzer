package com.example.facultyload.controller;

import com.example.facultyload.dto.admin.UpdateUserRoleRequest;
import com.example.facultyload.dto.admin.UserResponse;
import com.example.facultyload.entity.User;
import com.example.facultyload.service.UserAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/super-admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SuperAdminController {

    private final UserAdminService userAdminService;

    @PutMapping("/users/{email}/role")
    public UserResponse updateUserRole(@PathVariable String email, @Valid @RequestBody UpdateUserRoleRequest request) {
        User user = userAdminService.updateUserRoleByEmail(email, request.role());
        return new UserResponse(user.getId(), user.getEmail(), user.getRole().getName(), user.getIsActive());
    }
}

