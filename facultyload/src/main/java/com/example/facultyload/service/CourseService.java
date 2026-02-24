package com.example.facultyload.service;

import com.example.facultyload.dto.course.CourseCreateRequest;
import com.example.facultyload.dto.course.CourseResponse;

import java.util.List;

public interface CourseService {
    CourseResponse create(CourseCreateRequest request);
    CourseResponse get(Long id);
    List<CourseResponse> list();
}

