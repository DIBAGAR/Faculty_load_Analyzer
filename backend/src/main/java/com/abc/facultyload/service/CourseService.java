package com.abc.facultyload.service;

import com.abc.facultyload.entity.Course;
import com.abc.facultyload.entity.Department;
import com.abc.facultyload.exception.AppException;
import com.abc.facultyload.repository.CourseRepository;
import com.abc.facultyload.repository.DepartmentRepository;
import com.abc.facultyload.util.ExcelExporter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;
    private final ExcelExporter excelExporter;

    public List<Course> getAllCourses() {
        return courseRepository.findAllByActiveTrue();
    }

    public List<Course> getCoursesByDeptAndSemester(Long deptId, Integer semester) {
        return courseRepository.findAllByDepartmentIdAndSemester(deptId, semester);
    }

    @Transactional
    public Course createCourse(Map<String, Object> payload) {
        String courseCode = (String) payload.get("courseCode");
        Long parsedDeptId = Long.parseLong(payload.get("departmentId").toString());

        if (courseRepository.existsByCourseCodeAndDepartmentId(courseCode, parsedDeptId))
            throw new AppException("Course code already exists in this department", HttpStatus.BAD_REQUEST);

        Department dept = departmentRepository.findById(Long.parseLong(payload.get("departmentId").toString()))
                .orElseThrow(() -> new AppException("Department not found", HttpStatus.NOT_FOUND));

        Course course = Course.builder()
                .courseCode(courseCode)
                .courseName((String) payload.get("courseName"))
                .credit(payload.get("credit") != null ? Integer.parseInt(payload.get("credit").toString()) : null)
                .department(dept)
                .semester(Integer.parseInt(payload.get("semester").toString()))
                .active(true)
                .build();
        return courseRepository.save(course);
    }

    @Transactional
    public Course updateCourse(Long id, Map<String, Object> payload) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new AppException("Course not found", HttpStatus.NOT_FOUND));

        String newCourseCode = (String) payload.get("courseCode");
        Long parsedDeptId = Long.parseLong(payload.get("departmentId").toString());

        Department dept = departmentRepository.findById(parsedDeptId)
                .orElseThrow(() -> new AppException("Department not found", HttpStatus.NOT_FOUND));

        courseRepository.findByCourseCodeAndDepartmentId(newCourseCode, parsedDeptId)
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new AppException("Course code already exists in this department", HttpStatus.BAD_REQUEST);
                    }
                });

        course.setCourseCode((String) payload.get("courseCode"));
        course.setCourseName((String) payload.get("courseName"));
        course.setCredit(payload.get("credit") != null ? Integer.parseInt(payload.get("credit").toString()) : null);
        course.setDepartment(dept);
        course.setSemester(Integer.parseInt(payload.get("semester").toString()));
        return courseRepository.save(course);
    }

    @Transactional
    public void deleteCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new AppException("Course not found", HttpStatus.NOT_FOUND));
        courseRepository.delete(course);
    }

    public ByteArrayInputStream exportCoursesExcel() {
        List<Course> courses = courseRepository.findAllByActiveTrue();
        List<String> headers = List.of("Course Code", "Course Name", "Credit", "Department", "Semester");
        List<Map<String, Object>> data = courses.stream().map(c -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("Course Code", c.getCourseCode());
            row.put("Course Name", c.getCourseName());
            row.put("Credit", c.getCredit());
            row.put("Department", c.getDepartment().getDeptName());
            row.put("Semester", c.getSemester());
            return row;
        }).toList();
        return excelExporter.exportToExcel("Course List", headers, data);
    }
}
