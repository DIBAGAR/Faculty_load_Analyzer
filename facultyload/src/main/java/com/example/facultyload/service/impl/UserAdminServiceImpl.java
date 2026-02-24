package com.example.facultyload.service.impl;

import com.example.facultyload.entity.Role;
import com.example.facultyload.entity.RoleName;
import com.example.facultyload.entity.User;
import com.example.facultyload.exception.NotFoundException;
import com.example.facultyload.repository.RoleRepository;
import com.example.facultyload.repository.UserRepository;
import com.example.facultyload.service.UserAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserAdminServiceImpl implements UserAdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public User updateUserRoleByEmail(String email, RoleName roleName) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException("User not found: " + email));
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new NotFoundException("Role not found: " + roleName));
        user.setRole(role);
        return userRepository.save(user);
    }
}

