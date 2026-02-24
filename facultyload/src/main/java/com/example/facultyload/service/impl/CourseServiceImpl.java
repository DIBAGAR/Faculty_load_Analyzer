package com.example.facultyload.service.impl;

import com.example.facultyload.dto.course.CourseCreateRequest;
import com.example.facultyload.dto.course.CourseResponse;
import com.example.facultyload.entity.Course;
import com.example.facultyload.entity.Department;
import com.example.facultyload.exception.BadRequestException;
import com.example.facultyload.exception.NotFoundException;
import com.example.facultyload.repository.CourseRepository;
import com.example.facultyload.repository.DepartmentRepository;
import com.example.facultyload.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public CourseResponse create(CourseCreateRequest request) {
        if (courseRepository.existsByCourseCodeIgnoreCase(request.courseCode())) {
            throw new BadRequestException("Course code already exists: " + request.courseCode());
        }

        Set<Department> departments = new HashSet<>();
        if (request.departmentIds() != null) {
            for (Long deptId : request.departmentIds()) {
                departments.add(departmentRepository.findById(deptId)
                        .orElseThrow(() -> new NotFoundException("Department not found: " + deptId)));
            }
        }

        Course saved = courseRepository.save(Course.builder()
                .courseCode(request.courseCode().trim())
                .name(request.name().trim())
                .credit(request.credit())
                .departments(departments)
                .build());

        return toResponse(saved);
    }

    @Override
    public CourseResponse get(Long id) {
        return courseRepository.findById(id).map(this::toResponse)
                .orElseThrow(() -> new NotFoundException("Course not found: " + id));
    }

    @Override
    public java.util.List<CourseResponse> list() {
        return courseRepository.findAll().stream().map(this::toResponse).toList();
    }

    private CourseResponse toResponse(Course c) {
        Set<Long> deptIds = c.getDepartments().stream().map(Department::getId).collect(java.util.stream.Collectors.toSet());
        return new CourseResponse(c.getId(), c.getCourseCode(), c.getName(), c.getCredit(), deptIds);
    }
}

