package com.example.facultyload.config;

import com.example.facultyload.entity.Role;
import com.example.facultyload.entity.RoleName;
import com.example.facultyload.entity.User;
import com.example.facultyload.repository.RoleRepository;
import com.example.facultyload.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.superadmin.email:}")
    private String superAdminEmail;

    @Value("${app.bootstrap.superadmin.password:}")
    private String superAdminPassword;

    @Override
    public void run(String... args) {
        for (RoleName rn : RoleName.values()) {
            roleRepository.findByName(rn).orElseGet(() -> roleRepository.save(Role.builder().name(rn).build()));
        }

        if (superAdminEmail != null && !superAdminEmail.isBlank()
                && superAdminPassword != null && !superAdminPassword.isBlank()) {
            userRepository.findByEmailIgnoreCase(superAdminEmail).orElseGet(() -> {
                Role superRole = roleRepository.findByName(RoleName.SUPER_ADMIN).orElseThrow();
                return userRepository.save(User.builder()
                        .email(superAdminEmail.toLowerCase())
                        .password(passwordEncoder.encode(superAdminPassword))
                        .role(superRole)
                        .isActive(true)
                        .build());
            });
        }
    }
}

