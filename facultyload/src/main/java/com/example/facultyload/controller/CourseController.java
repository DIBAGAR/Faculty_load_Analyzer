package com.example.facultyload.controller;

import com.example.facultyload.dto.course.CourseCreateRequest;
import com.example.facultyload.dto.course.CourseResponse;
import com.example.facultyload.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PreAuthorize("hasRole('COURSE_ADMIN') or hasRole('SUPER_ADMIN')")
    @PostMapping
    public CourseResponse create(@Valid @RequestBody CourseCreateRequest request) {
        return courseService.create(request);
    }

    @GetMapping("/{id}")
    public CourseResponse get(@PathVariable Long id) {
        return courseService.get(id);
    }

    @GetMapping
    public List<CourseResponse> list() {
        return courseService.list();
    }
}

