package com.abc.facultyload.controller;

import com.abc.facultyload.entity.Course;
import com.abc.facultyload.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/course-admin")
@RequiredArgsConstructor
public class CourseAdminController {

    private final CourseService courseService;

    @GetMapping("/courses")
    public ResponseEntity<List<Course>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    @PostMapping("/courses")
    public ResponseEntity<Course> createCourse(@RequestBody Map<String, Object> payload) {
        return ResponseEntity.ok(courseService.createCourse(payload));
    }

    @PutMapping("/courses/{id}")
    public ResponseEntity<Course> updateCourse(@PathVariable("id") Long id, @RequestBody Map<String, Object> payload) {
        return ResponseEntity.ok(courseService.updateCourse(id, payload));
    }

    @DeleteMapping("/courses/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable("id") Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/courses/export")
    public ResponseEntity<InputStreamResource> exportExcel() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=course_list.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(courseService.exportCoursesExcel()));
    }

    // For timetable dropdowns - accessible by HOD role too
    @GetMapping("/courses/dept/{deptId}/semester/{semester}")
    public ResponseEntity<List<Course>> getCoursesByDeptSemester(@PathVariable("deptId") Long deptId, @PathVariable("semester") Integer semester) {
        return ResponseEntity.ok(courseService.getCoursesByDeptAndSemester(deptId, semester));
    }
}
