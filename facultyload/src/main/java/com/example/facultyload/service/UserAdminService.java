package com.example.facultyload.service;

import com.example.facultyload.entity.RoleName;
import com.example.facultyload.entity.User;

public interface UserAdminService {
    User updateUserRoleByEmail(String email, RoleName roleName);
}

