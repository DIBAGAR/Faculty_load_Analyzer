package com.abc.facultyload.service;

import com.abc.facultyload.entity.*;
import com.abc.facultyload.exception.AppException;
import com.abc.facultyload.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final FacultyRepository facultyRepository;
    private final CourseRepository courseRepository;
    private final VenueRepository venueRepository;
    private final PasswordEncoder passwordEncoder;

    // ===== Super Admin Dashboard Stats =====
    public Map<String, Object> getSuperAdminStats() {
        List<Department> departments = departmentRepository.findAll();
        Map<String, Object> stats = new LinkedHashMap<>();
        List<Map<String, Object>> deptStats = departments.stream().map(dept -> {
            Map<String, Object> ds = new LinkedHashMap<>();
            ds.put("deptId", dept.getId());
            ds.put("deptName", dept.getDeptName());
            ds.put("facultyCount", facultyRepository.countByDeptId(dept.getId()));
            ds.put("courseCount", courseRepository.countByDepartmentId(dept.getId()));
            ds.put("labCount", venueRepository.countByDepartmentIdAndVenueType(dept.getId(), Venue.VenueType.LAB));
            ds.put("classroomCount", venueRepository.countByDepartmentIdAndVenueType(dept.getId(), Venue.VenueType.CLASSROOM));
            return ds;
        }).toList();
        stats.put("departments", deptStats);
        stats.put("totalFaculty", facultyRepository.count());
        stats.put("totalCourses", courseRepository.count());
        stats.put("totalVenues", venueRepository.count());
        return stats;
    }

    // ===== Admin Management =====
    public List<Map<String, Object>> getAllAdmins() {
        List<Role.RoleName> adminRoles = List.of(
                Role.RoleName.FACULTY_ADMIN, Role.RoleName.DEPARTMENT_ADMIN,
                Role.RoleName.COURSE_ADMIN, Role.RoleName.VENUE_ADMIN
        );
        return userRepository.findAll().stream()
                .filter(u -> adminRoles.contains(u.getRole().getName()))
                .map(u -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", u.getId());
                    m.put("name", u.getName());
                    m.put("email", u.getEmail());
                    m.put("rollNumber", u.getRollNumber());
                    m.put("role", u.getRole().getName().name());
                    m.put("isActive", u.isActive());
                    return m;
                }).toList();
    }

    @Transactional
    public Map<String, Object> createAdmin(Map<String, Object> payload) {
        String email = (String) payload.get("email");
        String rollNumber = (String) payload.get("rollNumber");
        if (userRepository.existsByEmail(email))
            throw new AppException("Email already registered", HttpStatus.BAD_REQUEST);

        String roleStr = (String) payload.get("adminType");
        Role.RoleName roleName = Role.RoleName.valueOf(roleStr);
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new AppException("Role not found", HttpStatus.INTERNAL_SERVER_ERROR));

        User user = User.builder()
                .email(email)
                .rollNumber(rollNumber)
                .name((String) payload.get("name"))
                .passwordHash(passwordEncoder.encode((String) payload.get("password")))
                .role(role)
                .active(true)
                .build();
        user = userRepository.save(user);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", user.getId());
        result.put("name", user.getName());
        result.put("email", user.getEmail());
        result.put("role", user.getRole().getName().name());
        return result;
    }

    @Transactional
    public void deleteAdmin(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException("Admin not found", HttpStatus.NOT_FOUND));
        userRepository.delete(user);
    }
}
