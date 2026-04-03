package com.abc.facultyload.config;

import com.abc.facultyload.entity.Role;
import com.abc.facultyload.entity.User;
import com.abc.facultyload.repository.RoleRepository;
import com.abc.facultyload.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import jakarta.persistence.EntityManager;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;

    @Value("${superadmin.email}")
    private String superAdminEmail;

    @Value("${superadmin.password}")
    private String superAdminPassword;

    @Value("${superadmin.name}")
    private String superAdminName;

    @Value("${superadmin.rollNumber}")
    private String superAdminRollNumber;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // Initialize all roles
        Arrays.stream(Role.RoleName.values()).forEach(roleName -> {
            if (roleRepository.findByName(roleName).isEmpty()) {
                roleRepository.save(Role.builder().name(roleName).build());
                log.info("Created role: {}", roleName);
            }
        });

        // Initialize Super Admin
        userRepository.findByEmail(superAdminEmail).ifPresentOrElse(
            admin -> {
                admin.setPasswordHash(passwordEncoder.encode(superAdminPassword));
                admin.setName(superAdminName);
                admin.setRollNumber(superAdminRollNumber);
                admin.setActive(true);
                userRepository.save(admin);
                log.info("Updated Super Admin account password: {}", superAdminEmail);
            },
            () -> {
                Role superAdminRole = roleRepository.findByName(Role.RoleName.SUPER_ADMIN)
                        .orElseThrow(() -> new RuntimeException("Super Admin role not found"));

                User superAdmin = User.builder()
                        .email(superAdminEmail)
                        .rollNumber(superAdminRollNumber)
                        .passwordHash(passwordEncoder.encode(superAdminPassword))
                        .role(superAdminRole)
                        .name(superAdminName)
                        .active(true)
                        .build();

                userRepository.save(superAdmin);
                log.info("Created Super Admin account: {}", superAdminEmail);
            }
        );

        // Cleanup old restrictive constraints for course_code to allow multiple departments
        try {
            entityManager.createNativeQuery("ALTER TABLE courses DROP CONSTRAINT IF EXISTS uk_qivqhvw6mxtb9v7ry9hlyshab CASCADE").executeUpdate();
            entityManager.createNativeQuery("ALTER TABLE courses DROP CONSTRAINT IF EXISTS courses_course_code_key CASCADE").executeUpdate();
            log.info("Successfully dropped old restrictive course_code unique constraints if they existed.");
        } catch (Exception e) {
            log.warn("Could not drop old course_code constraints. They might already be removed: {}", e.getMessage());
        }

        // Ensure leave_type column exists (added for OnDuty tracking)
        try {
            entityManager.createNativeQuery(
                "ALTER TABLE leave_requests ADD COLUMN IF NOT EXISTS leave_type VARCHAR(15) DEFAULT 'LEAVE'"
            ).executeUpdate();
            log.info("Ensured leave_type column exists in leave_requests.");
        } catch (Exception e) {
            log.warn("leave_type column migration skipped: {}", e.getMessage());
        }

        // Ensure attendance_start_date column exists in departments
        try {
            entityManager.createNativeQuery(
                "ALTER TABLE departments ADD COLUMN IF NOT EXISTS attendance_start_date DATE"
            ).executeUpdate();
            log.info("Ensured attendance_start_date column exists in departments.");
        } catch (Exception e) {
            log.warn("attendance_start_date column migration skipped: {}", e.getMessage());
        }

        log.info("Data initialization complete.");
    }
}
