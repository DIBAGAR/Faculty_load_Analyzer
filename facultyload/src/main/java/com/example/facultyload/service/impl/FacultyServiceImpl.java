package com.example.facultyload.service.impl;

import com.example.facultyload.dto.faculty.AssignHodRequest;
import com.example.facultyload.dto.faculty.FacultyCreateRequest;
import com.example.facultyload.dto.faculty.FacultyResponse;
import com.example.facultyload.entity.*;
import com.example.facultyload.exception.BadRequestException;
import com.example.facultyload.exception.NotFoundException;
import com.example.facultyload.repository.CourseRepository;
import com.example.facultyload.repository.DepartmentRepository;
import com.example.facultyload.repository.FacultyRepository;
import com.example.facultyload.repository.RoleRepository;
import com.example.facultyload.repository.UserRepository;
import com.example.facultyload.service.FacultyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FacultyServiceImpl implements FacultyService {

    private final FacultyRepository facultyRepository;
    private final DepartmentRepository departmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public FacultyResponse create(FacultyCreateRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BadRequestException("Email already in use: " + email);
        }

        Department department = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new NotFoundException("Department not found: " + request.departmentId()));

        Role facultyRole = roleRepository.findByName(RoleName.FACULTY)
                .orElseThrow(() -> new NotFoundException("Role not found: FACULTY"));

        User user = userRepository.save(User.builder()
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .role(facultyRole)
                .isActive(true)
                .build());

        Faculty faculty = Faculty.builder()
                .user(user)
                .department(department)
                .name(request.name().trim())
                .designation(request.designation())
                .isHod(false)
                .monthlyAssignedHours(0)
                .totalAssignedHours(0)
                .isActive(true)
                .build();

        Faculty saved = facultyRepository.save(faculty);

        Set<Long> primary = request.primaryCourseIds() == null ? Set.of() : request.primaryCourseIds();
        Set<Long> additional = request.additionalCourseIds() == null ? Set.of() : request.additionalCourseIds();
        if (!primary.isEmpty() || !additional.isEmpty()) {
            attachCourses(saved, department.getId(), primary, CourseKnowledgeType.PRIMARY);
            attachCourses(saved, department.getId(), additional, CourseKnowledgeType.ADDITIONAL);
            saved = facultyRepository.save(saved);
        }

        return toResponse(saved);
    }

    @Override
    public FacultyResponse get(Long id) {
        return facultyRepository.findById(id).map(this::toResponse)
                .orElseThrow(() -> new NotFoundException("Faculty not found: " + id));
    }

    @Override
    public java.util.List<FacultyResponse> listByDepartment(Long departmentId) {
        return facultyRepository.findByDepartmentIdAndIsActiveTrue(departmentId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public FacultyResponse assignHod(Long departmentId, AssignHodRequest request) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new NotFoundException("Department not found: " + departmentId));
        Faculty newHod = facultyRepository.findById(request.facultyId())
                .orElseThrow(() -> new NotFoundException("Faculty not found: " + request.facultyId()));

        if (!newHod.getDepartment().getId().equals(department.getId())) {
            throw new BadRequestException("HOD must be from the same department");
        }

        Role facultyRole = roleRepository.findByName(RoleName.FACULTY).orElseThrow();
        Role hodRole = roleRepository.findByName(RoleName.HOD).orElseThrow();

        Faculty oldHod = department.getHod();
        if (oldHod != null && !oldHod.getId().equals(newHod.getId())) {
            oldHod.setHod(false);
            oldHod.getUser().setRole(facultyRole);
            facultyRepository.save(oldHod);
        }

        newHod.setHod(true);
        newHod.getUser().setRole(hodRole);
        facultyRepository.save(newHod);

        department.setHod(newHod);
        departmentRepository.save(department);

        return toResponse(newHod);
    }

    private void attachCourses(Faculty faculty, Long departmentId, Set<Long> courseIds, CourseKnowledgeType type) {
        Set<FacultyCourse> toAdd = new HashSet<>();
        for (Long courseId : courseIds) {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new NotFoundException("Course not found: " + courseId));
            boolean allowed = course.getDepartments().stream().anyMatch(d -> d.getId().equals(departmentId));
            if (!allowed) {
                throw new BadRequestException("Course " + course.getCourseCode() + " is not linked to this department");
            }

            FacultyCourse fc = FacultyCourse.builder()
                    .id(new FacultyCourseId(faculty.getId(), course.getId()))
                    .faculty(faculty)
                    .course(course)
                    .knowledgeType(type)
                    .build();
            toAdd.add(fc);
        }
        faculty.getCoursesKnown().addAll(toAdd);
    }

    private FacultyResponse toResponse(Faculty f) {
        Set<Long> primary = new HashSet<>();
        Set<Long> additional = new HashSet<>();
        if (f.getCoursesKnown() != null) {
            for (FacultyCourse fc : f.getCoursesKnown()) {
                if (fc.getKnowledgeType() == CourseKnowledgeType.PRIMARY) {
                    primary.add(fc.getCourse().getId());
                } else if (fc.getKnowledgeType() == CourseKnowledgeType.ADDITIONAL) {
                    additional.add(fc.getCourse().getId());
                }
            }
        }

        return new FacultyResponse(
                f.getId(),
                f.getName(),
                f.getUser().getEmail(),
                f.getDepartment().getId(),
                f.getDesignation(),
                f.isHod(),
                f.getMonthlyAssignedHours(),
                f.getTotalAssignedHours(),
                primary,
                additional
        );
    }
}

